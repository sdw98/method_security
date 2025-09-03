package com.sdw98.method_security.service;

import com.sdw98.method_security.model.User;
import com.sdw98.method_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdvancedSecurityService {
    private final UserRepository userRepository;

    @PreAuthorize("@customSecurity.isBusinessHours() or hasRole('ADMIN')")
    public void performSensitiveOperation() {
        log.info("⚠️ 민감한 작업 실행 - 업무 시간 또는 관리자");
    }

    @PreAuthorize("@customSecurity.isDepartmentMember(T(com.sdw98.method_security.model.Department).HR, authentication")
    public List<User> getEmployeeRecords() {
        log.info("👥 직원 기록 조회 - HR 부서원만");
        return userRepository.findAll();
    }

    @PreAuthorize("@customSecurity.isManagerOrHigher(authentication)")
    public void approveRequest(Long requestId) {
        log.info("✅ 요청 승인 - 매니저 이상");
    }

    @PreAuthorize("@customSecurity.isActiveUser(authentication) and " +
            "@customSecurity.hasMinimumPosts(5, authentication)")
    public void accessPremiumFeature() {
        log.info("💎 프리미엄 기능 - 활성 사용자이면서 게시글 5개 이상");
    }

    @PreAuthorize("@customSecurity.isBusinessHours() and " +
            "@customSecurity.isDepartmentMember(T(com.sdw98.method_security.model.Department).FINANCE, authentication) and " +
            "@customSecurity.isManagerOrHigher(authentication)")
    public void processPayroll() {
        log.info("💰 급여 처리 - 업무시간 + 재무부서 + 매니저 이상");
    }
}