package com.sdw98.method_security.dto;

import com.sdw98.method_security.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedPageDto {
    private UserDto currentUser;
    private boolean canPerformSensitive;
    private boolean canAccessHR;
    private boolean canAccessPremium;
    private int userPostCount;
}