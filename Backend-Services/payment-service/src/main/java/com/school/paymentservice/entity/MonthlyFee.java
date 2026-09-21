package com.school.paymentservice.entity;

import com.school.common.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Entity
@Table(name = "monthly_fees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "month", nullable = false)
    private String monthYear; // Format: "YYYY-MM" (e.g., "2026-09")

    @Column(name = "base_amount", nullable = false)
    @Default
    private Double baseAmount = 0.0;

    @Column(name = "waiver_amount")
    @Default
    private Double waiverAmount = 0.0;

    @Column(name = "penalty_amount", nullable = false)
    @Default
    private Double penaltyAmount = 0.0; // Carried forward from previous month shortfall

    @Column(name = "total_payable", nullable = false)
    @Default
    private Double totalPayable = 0.0; // (baseAmount + penaltyAmount) - waiverAmount

    @Column(name = "base_amount", nullable = false)
    @Default
    private Double paidAmount = 0.0;

    @Column(name = "base_amount", nullable = false)
    private PaymentStatus status; // "PENDING", "PARTIAL", "PAID", "EXEMPT"
}