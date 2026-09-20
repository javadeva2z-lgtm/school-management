package com.school.paymentservice.service;

import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.entity.Payment;
import com.school.paymentservice.entity.Fee;
import com.school.paymentservice.repository.PaymentRepository;
import com.school.paymentservice.repository.FeeRepository;
import com.school.paymentservice.converter.PaymentConverter;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final FeeRepository feeRepository;
    private final PaymentConverter paymentConverter;

    public PaymentDTO recordPayment(PaymentDTO paymentDTO) {
        log.info("Recording payment for fee: {} student: {}", paymentDTO.getFeeId(), paymentDTO.getStudentId());
        
        Fee fee = feeRepository.findById(paymentDTO.getFeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee", "id", paymentDTO.getFeeId()));

        Payment payment = paymentConverter.dtoToEntity(paymentDTO);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment = paymentRepository.save(payment);
        
        // Update fee status
        updateFeeStatus(fee, payment);
        
        log.info("Payment recorded successfully with id: {}", payment.getId());
        return paymentConverter.entityToDTO(payment);
    }

    private void updateFeeStatus(Fee fee, Payment payment) {
        BigDecimal totalPaid = paymentRepository.findByFeeId(fee.getId())
                .stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(fee.getAmount()) >= 0) {
            fee.setStatus("PAID");
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            fee.setStatus("PARTIAL");
        }
        feeRepository.save(fee);
    }

    public PaymentDTO getPaymentById(Long id) {
        log.info("Fetching payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return paymentConverter.entityToDTO(payment);
    }

    public List<PaymentDTO> getStudentPayments(Long studentId) {
        log.info("Fetching payments for student: {}", studentId);
        List<Payment> payments = paymentRepository.findByStudentId(studentId);
        return payments.stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public Page<PaymentDTO> getStudentPaymentsPaginated(Long studentId, Pageable pageable) {
        log.info("Fetching paginated payments for student: {}", studentId);
        Page<Payment> payments = paymentRepository.findByStudentId(studentId, pageable);
        return payments.map(paymentConverter::entityToDTO);
    }

    public List<PaymentDTO> getFeePayments(Long feeId) {
        log.info("Fetching payments for fee: {}", feeId);
        List<Payment> payments = paymentRepository.findByFeeId(feeId);
        return payments.stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        log.info("Fetching payment with transaction id: {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
        return paymentConverter.entityToDTO(payment);
    }

    public List<PaymentDTO> getPaymentsByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching payments between {} and {}", fromDate, toDate);
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(fromDate, toDate);
        return payments.stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        log.info("Updating payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        payment.setRemarks(paymentDTO.getRemarks());
        payment.setReceiptUrl(paymentDTO.getReceiptUrl());
        
        payment = paymentRepository.save(payment);
        log.info("Payment updated successfully with id: {}", payment.getId());
        return paymentConverter.entityToDTO(payment);
    }

    public void deletePayment(Long id) {
        log.info("Deleting payment with id: {}", id);
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment", "id", id);
        }
        paymentRepository.deleteById(id);
        log.info("Payment deleted successfully with id: {}", id);
    }

    public BigDecimal getTotalPaymentsByStudent(Long studentId) {
        log.info("Calculating total payments for student: {}", studentId);
        return paymentRepository.findByStudentId(studentId)
                .stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
