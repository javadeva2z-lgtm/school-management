package com.school.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassDTO {
    private Long id;
    private Long classId;
    
    @NotBlank(message = "Class name is required")
    private String className;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    private Boolean isActive;
}
