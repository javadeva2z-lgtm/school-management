package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {
    private Long id;
    private String username;
    private String token;
    private String refreshToken;
    private java.util.List<String> roles;
    private boolean isActive;
}
