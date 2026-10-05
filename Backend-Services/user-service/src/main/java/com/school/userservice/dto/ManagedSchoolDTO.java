package com.school.userservice.dto;

public record ManagedSchoolDTO(
        Long id,
        String schoolCode,
        String schoolName,
        String address,
        String phone,
        String email,
        String website,
        String principalName,
        String announcement,
        String keywords,
        Boolean isActive) {
}
