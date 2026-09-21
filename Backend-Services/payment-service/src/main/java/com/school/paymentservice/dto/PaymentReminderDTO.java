package com.school.paymentservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.school.common.enums.PaymentReminderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentReminderDTO {
    private Long id;
    private Long monthlyFeeId;
    private Long studentId;
    private PaymentReminderType reminderType;
    private Double amount;
    private LocalDate dueDate;
    private Boolean sent;
    private LocalDateTime sentAt;
}
