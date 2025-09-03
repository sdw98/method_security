package com.sdw98.method_security.dto;

import com.sdw98.method_security.model.Department;
import com.sdw98.method_security.model.Role;
import com.sdw98.method_security.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private Department department;
    private boolean enabled;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .enabled(user.isEnabled())
                .build();
    }
}