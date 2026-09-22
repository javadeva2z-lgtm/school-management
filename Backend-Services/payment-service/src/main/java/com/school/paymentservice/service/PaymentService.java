package com.school.paymentservice.service;

import com.school.common.enums.PaymentStatus;
import com.school.common.exception.ResourceNotFoundException;
import com.school.paymentservice.converter.PaymentConverter;
import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.entity.Payment;
import com.school.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentConverter paymentConverter;

    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        log.info("Recording payment for student {} amount {}", paymentDTO.getStudentId(), paymentDTO.getAmountPaid());
        Payment payment = paymentConverter.dtoToEntity(paymentDTO);
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PAID);
        }
        Payment saved = paymentRepository.save(payment);
        return paymentConverter.entityToDTO(saved);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return paymentConverter.entityToDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStudent(Long studentId) {
        return paymentRepository.findByStudentId(studentId).stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByStudent(Long studentId, Pageable pageable) {
        return paymentRepository.findByStudentId(studentId, pageable)
                .map(paymentConverter::entityToDTO);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByMonthlyFee(Long monthlyFeeId) {
        return paymentRepository.findByMonthlyFeeId(monthlyFeeId).stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));
        return paymentConverter.entityToDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return paymentRepository.findByPaymentDateBetween(fromDate, toDate).stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public PaymentDTO updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        payment.setStatus(status);
        return paymentConverter.entityToDTO(paymentRepository.save(payment));
    }

    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment", "id", id);
        }
        paymentRepository.deleteById(id);
    }
}
