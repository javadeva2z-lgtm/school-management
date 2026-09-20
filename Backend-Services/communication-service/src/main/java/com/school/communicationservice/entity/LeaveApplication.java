package com.school.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.school.common.enums.LeaveStatus;

@Entity
@Table(name = "leave_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LeaveApplication {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "admission_number", nullable = false)
	private Long admissionNumber;
	@Column(nullable = false, columnDefinition = "TEXT")
	private String reason;
	@Column(name = "from_date", nullable = false)
	private LocalDate fromDate;
	@Column(name = "to_date", nullable = false)
	private LocalDate toDate;
	@Column(name = "total_days", nullable = false)
	private Integer totalDays;
	@Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
	private LeaveStatus status;
	@Column(name = "approved_by")
	private String approvedBy;
	@Column(name = "approval_date")
	private LocalDateTime approvalDate;
	private String remarks;
	@Column(name = "created_at", updatable = false)
	@CreatedDate
	private LocalDateTime createdAt;
	@Column(name = "updated_at")
	@LastModifiedDate
	private LocalDateTime updatedAt;
}
