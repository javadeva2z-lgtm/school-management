package com.school.userservice.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PasswordManagedUserDTO {
    String username;
    String role;
    String displayName;
}
