package com.school.communicationservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.*;

import com.school.common.enums.LeaveStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplicationDTO {
	private Long id;
	@NotNull
	private Long admissionNumber;
	@NotBlank
	private String reason;
	@NotNull
	private LocalDate fromDate;
	@NotNull
	private LocalDate toDate;
	private Integer totalDays;
	private LeaveStatus status;
	private String approvedBy;
	private LocalDateTime approvalDate;
	private String remarks;
}
