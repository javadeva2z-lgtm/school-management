package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;
    private String gender;
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Username is required")
    private String username;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private String qualification;
    private String specialization;
    private LocalDate joiningDate;
    private Integer experienceYears;
    private String address;
    @NotBlank(message = "Phone number is required")
    private String phone;
}
