package com.school.paymentservice.repository;

import com.school.paymentservice.entity.PaymentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {
    List<PaymentReminder> findByStudentId(Long studentId);
    List<PaymentReminder> findByFeeId(Long feeId);
    List<PaymentReminder> findByReminderDateAndIsSentFalse(LocalDate reminderDate);
    List<PaymentReminder> findByIsSentFalse();
    List<PaymentReminder> findByStudentIdAndIsSentFalse(Long studentId);
}
