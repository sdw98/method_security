package com.sdw98.method_security.service;

import com.sdw98.method_security.model.Department;
import com.sdw98.method_security.model.Role;
import com.sdw98.method_security.model.User;
import com.sdw98.method_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Secured("ROLE_ADMIN")
    public List<User> getAllUsers() {
        log.info("🔍 [SECURED] 모든 사용자 조회 - ADMIN 권한 필요");
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and #user.department == authentication.principal.department)")
    public User createUser(User user) {
        log.info("✏️ [PRE_AUTHORIZE] 사용자 생성 - 관리자 또는 동일 부서 매니저");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public User updateUser(Long userId, User updatedUser) {
        log.info("✏️ [PRE_AUTHORIZE] 사용자 정보 수정 - 본인 또는 관리자");
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        existingUser.setEmail(updatedUser.getEmail());
        return userRepository.save(existingUser);
    }

    @PostAuthorize("returnObject == null or " +
            "returnObject.id == authentication.principal.id or " +
            "returnObject.department == authentication.principal.department or " +
            "hasRole('ADMIN')"
    )
    public User getUserById(Long userId) {
        log.info("🔍 [POST_AUTHORIZE] 사용자 조회 - 리턴 후 권한 검증");
        return userRepository.findById(userId).orElse(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        log.info("🗑️ [PRE_AUTHORIZE] 사용자 삭제 - ADMIN 권한 필요");
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('MANAGER') and @userService.isSameDepartment(#userId, authentication))"
    )
    public void deactivateUser (Long userId) {
        log.info("⏸️ [PRE_AUTHORIZE] 사용자 비활성화 - 부서 확인");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public boolean isSameDepartment(Long userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        User targetUser = userRepository.findById(userId).orElse(null);

        return targetUser != null && currentUser.getDepartment() == targetUser.getDepartment();
    }

    public User registerUser(String username, String password, String email, Role role, Department department) {
        log.info("📝 사용자 등록 - 인증 불필요");

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role(role)
                .department(department)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}