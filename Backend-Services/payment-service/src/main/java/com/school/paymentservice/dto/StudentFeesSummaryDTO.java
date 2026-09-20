package com.school.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentFeesSummaryDTO {
    private Long studentId;
    private String studentName;
    private String academicYear;
    private BigDecimal totalFees;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private Integer totalFeeCount;
    private Integer paidFeeCount;
    private Integer pendingFeeCount;
    private Integer overdueCount;
    private String overallStatus; // UP_TO_DATE, PARTIALLY_PAID, OVERDUE
}
