package com.school.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;

    @NotNull(message = "Fee ID is required")
    private Long feeId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amountPaid;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, UPI, BANK_TRANSFER, CHEQUE

    private String transactionId;

    @NotNull(message = "Payment date is required")
    private LocalDateTime paymentDate;

    private String receiptUrl;

    private String status; // COMPLETED, PENDING, FAILED, REFUNDED

    private String remarks;
}
