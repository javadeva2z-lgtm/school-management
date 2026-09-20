package com.school.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentReminderDTO {
    private Long id;
    private Long feeId;
    private Long studentId;
    private String reminderType;
    private LocalDate reminderDate;
    private Boolean isSent;
    private String feeType;
    private BigDecimal amount;
    private LocalDate dueDate;
}
