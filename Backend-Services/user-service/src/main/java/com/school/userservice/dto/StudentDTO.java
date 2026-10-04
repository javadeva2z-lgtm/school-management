package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentDTO {
    private String name;
    private String gender;
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long admissionNumber;

    @NotNull(message = "Roll number is required")
    private Long rollNumber;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Section Name is required")
    private String sectionName;

    private String fatherName;
    private String motherName;
    private LocalDate dateOfBirth;

    @NotNull(message = "Admission date is required")
    @PastOrPresent(message = "Admission date cannot be in the future")
    private LocalDate admissionDate;

    private String address;
    private String parentPhone;
    private Boolean isEws;
}
