package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SectionDTO {
    private Long id;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotBlank(message = "Section name is required")
    private String sectionName;

    private Integer capacity;
    private Boolean isActive;
}
