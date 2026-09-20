package com.school.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "fee_id", nullable = false)
    private Long feeId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "reminder_type", nullable = false)
    private String reminderType; // DUE_DATE, OVERDUE_7DAYS, OVERDUE_14DAYS

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(name = "is_sent")
    private Boolean isSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
