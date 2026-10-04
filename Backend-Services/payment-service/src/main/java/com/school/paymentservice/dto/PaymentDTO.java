package com.school.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.school.common.enums.PaymentStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;

    @NotNull
    @Positive
    private Long admissionNumber;

    private Long monthlyFeeId;

    private String monthYear;

    private String transactionId;

    private String paymentMethod; // "UPI", "CASH", "CREDIT_CARD", "NET_BANKING"

    private Double amountPaid;

    private LocalDateTime paymentDate;

    private PaymentStatus status;
}
