package com.school.paymentservice.repository;

import com.school.common.enums.PaymentStatus;
import com.school.paymentservice.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAdmissionNumber(Long admissionNumber);

    Page<Payment> findByAdmissionNumber(Long admissionNumber, Pageable pageable);

    List<Payment> findByMonthlyFeeId(Long monthlyFeeId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaymentDateBetween(LocalDateTime fromDate, LocalDateTime toDate);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByAdmissionNumberAndStatus(Long admissionNumber, PaymentStatus status);
}
