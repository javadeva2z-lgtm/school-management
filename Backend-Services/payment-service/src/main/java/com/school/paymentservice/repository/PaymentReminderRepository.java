package com.school.paymentservice.repository;

import com.school.paymentservice.entity.PaymentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {
    List<PaymentReminder> findByStudentId(Long studentId);

    List<PaymentReminder> findByMonthlyFeeId(Long feeId);

    List<PaymentReminder> findByDueDateAndSentFalse(LocalDate reminderDate);

    List<PaymentReminder> findBySentFalse();

    List<PaymentReminder> findByStudentIdAndSentFalse(Long studentId);
}
