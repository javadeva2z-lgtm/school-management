package com.school.academicservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultDTO {
    private Long id;

    @NotNull(message = "Admission number is required")
    private Long admissionNumber;

    @NotNull(message = "Exam schedule ID is required")
    private Long examScheduleId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Marks obtained is required")
    @Min(value = 0, message = "Marks cannot be negative")
    private Integer marksObtained;

    private Integer outOf;

    private String grade;

    private String fileUrl;
}
