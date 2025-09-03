package com.sdw98.method_security.config;

import com.sdw98.method_security.model.*;
import com.sdw98.method_security.repository.PostRepository;
import com.sdw98.method_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if(!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .email("admin@company.com")
                    .role(Role.ADMIN)
                    .department(Department.IT)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }

        if(!userRepository.existsByUsername("manager")) {
            User manager = User.builder()
                    .username("manager")
                    .password(passwordEncoder.encode("manager"))
                    .email("manager@company.com")
                    .role(Role.MANAGER)
                    .department(Department.HR)
                    .enabled(true)
                    .build();
            userRepository.save(manager);
        }

        if(!userRepository.existsByUsername("hr_user")) {
            User hrUser = User.builder()
                    .username("hr_user")
                    .password(passwordEncoder.encode("hr123"))
                    .email("hr@company.com")
                    .role(Role.USER)
                    .department(Department.HR)
                    .enabled(true)
                    .build();
            userRepository.save(hrUser);
        }

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .email("user@company.com")
                    .role(Role.USER)
                    .department(Department.MARKETING)
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }

        if(postRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").get();
            User user = userRepository.findByUsername("user").get();

            Post publicPost = Post.builder()
                    .title("공개 게시글")
                    .content("누구나 볼 수 있는 게시글입니다.")
                    .author(admin)
                    .build();
            publicPost.setPublic(true);
            publicPost.setStatus(Status.PUBLISHED);
            postRepository.save(publicPost);

            Post privatePost = Post.builder()
                    .title("비공개 게시글")
                    .content("작성자만 볼 수 있는 게시글입니다.")
                    .author(user)
                    .build();
            publicPost.setPublic(false);
            postRepository.save(publicPost);
        }


        log.info("=== 초기 데이터 로딩 완료 ===");
        log.info("계정 정보:");
        log.info("관리자: admin/admin (ADMIN, IT부서)");
        log.info("매니저: manager/manager (MANAGER, HR부서)");
        log.info("사용자: user/user (USER, MARKETING부서)");
        log.info("HR사용자: hr_user/hr123 (USER, HR부서)");
    }
}