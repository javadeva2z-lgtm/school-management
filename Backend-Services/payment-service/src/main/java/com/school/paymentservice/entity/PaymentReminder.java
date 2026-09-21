package com.school.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.school.common.enums.PaymentReminderType;

@Entity
@Table(name = "payment_reminders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monthly_fee_id", nullable = false)
    private Long monthlyFeeId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "reminder_type", nullable = false)
    private PaymentReminderType reminderType;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "is_sent")
    @Default
    private Boolean sent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
