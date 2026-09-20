package com.school.paymentservice.repository;

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
    List<Payment> findByStudentId(Long studentId);
    Page<Payment> findByStudentId(Long studentId, Pageable pageable);
    List<Payment> findByFeeId(Long feeId);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByPaymentDateBetween(LocalDateTime fromDate, LocalDateTime toDate);
    List<Payment> findByStatus(String status);
    List<Payment> findByStudentIdAndStatus(Long studentId, String status);
}
