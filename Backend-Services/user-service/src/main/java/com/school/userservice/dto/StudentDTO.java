package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentDTO {
    private Long id;
    private String name;
    private String gender;
    private String email;

    @NotNull(message = "admissionNumber is required")
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
    private String address;
    private String parentPhone;
}
