package com.sdw98.method_security.controller;

import com.sdw98.method_security.dto.AdvancedPageDto;
import com.sdw98.method_security.dto.CreatePostRequest;
import com.sdw98.method_security.dto.UserDto;
import com.sdw98.method_security.model.Department;
import com.sdw98.method_security.model.Post;
import com.sdw98.method_security.model.Role;
import com.sdw98.method_security.model.User;
import com.sdw98.method_security.service.AdvancedSecurityService;
import com.sdw98.method_security.service.PostService;
import com.sdw98.method_security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;
    private final PostService postService;
    private AdvancedSecurityService advancedSecurityService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam Role role,
            @RequestParam Department department,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.registerUser(username, password, email, role, department);
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User currentUser = (User) authentication.getPrincipal();

        model.addAttribute("currentUser", currentUser);

        List<Post> publicPosts = postService.getPublicPosts();
        model.addAttribute("publicPosts", publicPosts);

        return "dashboard";
    }

    @GetMapping("/users")
    public String users(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
            return "users";
        } catch(AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", "사용자 목록을 조회할 권한이 없습니다. (ADMIN 권한 필요)");
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/posts")
    public String posts(Authentication authentication, Model model) {
        User currentUser = (User) authentication.getPrincipal();

        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", currentUser);

        return "posts";
    }

    @GetMapping("/posts/new")
    public String newPost() {
        return "post-form";
    }

    @PostMapping("/posts")
    public String createPost(
            @Valid @ModelAttribute CreatePostRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            postService.createPost(request.getTitle(), request.getContent(), currentUser, request.isPublic());
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 생성되었습니다.");
            return "redirect:/posts";
        } catch(Exception e) {
            redirectAttributes.addFlashAttribute("error", "게시글 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/posts/new";
        }
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id);
            redirectAttributes.addFlashAttribute("message", "게시글 삭제되었습니다");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", "게시글을 삭제할 권한이 없습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "게시글 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/posts";
    }

    @GetMapping("/advanced")
    public String advanced(Authentication authentication, Model model) {
        User currentUser = (User) authentication.getPrincipal();

        boolean canPerformSensitive = currentUser.getRole() == Role.ADMIN;
        boolean canAccessHR = currentUser.getDepartment() == Department.HR;

        int postCount = postService.getPostsByAuthor(currentUser).size();
        boolean canAccessPremium = postCount >= 5;

        AdvancedPageDto pageDto = AdvancedPageDto.builder()
                .currentUser(UserDto.fromEntity(currentUser))
                .canPerformSensitive(canPerformSensitive)
                .canAccessHR(canAccessHR)
                .canAccessPremium(canAccessPremium)
                .userPostCount(postCount)
                .build();

        model.addAttribute("pageDate", pageDto);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("canPerformSensitive", canPerformSensitive);
        model.addAttribute("canAccessHR", canAccessHR);
        model.addAttribute("canAccessPremium", canAccessPremium);

        return "advanced";
    }

    @PostMapping("/advanced/sensitive-operation")
    public String performSensitiveOperation(RedirectAttributes redirectAttributes) {
        try {
            advancedSecurityService.performSensitiveOperation();
            redirectAttributes.addFlashAttribute("message", "민감한 작업이 성공적으로 실행되었습니다.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", "업무 시간(9-17시)에만 실행 가능하거나 관리자 권한이 필요합니다.");
        }
        return "redirect:/advanced";
    }

    @PostMapping("/advanced/hr-records")
    public String accessHRRecords(Authentication authentication, RedirectAttributes redirectAttributes, Model model) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            model.addAttribute("currentUser", currentUser);

            boolean canPerformSensitive = currentUser.getRole() == Role.ADMIN;
            boolean canAccessHR = currentUser.getDepartment() == Department.HR;
            boolean canAccessPremium = false;

            model.addAttribute("canPerformSensitive", canPerformSensitive);
            model.addAttribute("canAccessHR", canAccessHR);
            model.addAttribute("canAccessPremium", canAccessPremium);

            List<User> employeeRecords = advancedSecurityService.getEmployeeRecords();
            model.addAttribute("employeeRecords", employeeRecords);
            model.addAttribute("message", "HR 기록을 성공적으로 조회했습니다.");
            return "advanced";
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", "HR 부서원만 접근 가능할 수 있습니다.");
            return "redirect:/advanced";
        }
    }

    @PostMapping("/advanced/premium-feature")
    public String accessPremiumFeature(RedirectAttributes redirectAttributes) {
        try {
            advancedSecurityService.accessPremiumFeature();
            redirectAttributes.addFlashAttribute("message", "프리미엄 기능에 접근했습니다.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("error", "활성 사용자이면서 게시글 5개 이상 작성해야 합니다.");
        }
        return "redirect:/advanced";
    }
}