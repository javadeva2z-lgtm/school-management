package com.school.paymentservice.dto;

import com.school.common.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFeeDTO {
    private Long id;
    private Long studentId;
    private String monthYear;
    private Double baseAmount;
    private Double waiverAmount;
    private Double penaltyAmount;
    private Double totalPayable;
    private Double paidAmount;
    private PaymentStatus status; // "PENDING", "PARTIAL", "PAID", "EXEMPT"
}