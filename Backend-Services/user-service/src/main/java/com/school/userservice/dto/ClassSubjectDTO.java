package com.school.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassSubjectDTO {
    private Long id;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotBlank(message = "Subject name is required")
    private String subjectName;

    @NotBlank(message = "Subject code is required")
    private String subjectCode;

    private Boolean isActive;
}
