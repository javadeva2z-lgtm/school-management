package com.school.paymentservice.service;

import com.school.common.enums.PaymentStatus;
import com.school.common.exception.ResourceNotFoundException;
import com.school.paymentservice.converter.PaymentConverter;
import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.entity.Payment;
import com.school.paymentservice.entity.MonthlyFee;
import com.school.paymentservice.repository.MonthlyFeeRepository;
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
    private final MonthlyFeeRepository monthlyFeeRepository;

    public PaymentDTO createPayment(PaymentDTO paymentDTO, Long verifiedStudentId) {
        log.info("Recording payment for admission number {} amount {}",
                paymentDTO.getAdmissionNumber(), paymentDTO.getAmountPaid());
        Payment payment = paymentConverter.dtoToEntity(paymentDTO);
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PAID);
        }

        MonthlyFee monthlyFee = null;
        if (payment.getStatus() == PaymentStatus.PAID) {
            if (payment.getAdmissionNumber() == null || payment.getMonthlyFeeId() == null
                    || verifiedStudentId == null
                    || payment.getMonthYear() == null || payment.getTransactionId() == null
                    || payment.getTransactionId().isBlank() || payment.getPaymentMethod() == null
                    || payment.getPaymentMethod().isBlank()) {
                throw new IllegalArgumentException("A paid transaction must include its admission number, fee, month, "
                        + "transaction id, and payment method");
            }
            if (payment.getAmountPaid() == null || !Double.isFinite(payment.getAmountPaid())
                    || payment.getAmountPaid() <= 0) {
                throw new IllegalArgumentException("A paid transaction must have a positive amount");
            }

            monthlyFee = monthlyFeeRepository.findById(payment.getMonthlyFeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "MonthlyFee", "id", payment.getMonthlyFeeId()));
            if (!monthlyFee.getStudentId().equals(verifiedStudentId)
                    || !monthlyFee.getMonthYear().equals(payment.getMonthYear())) {
                throw new IllegalArgumentException("Payment student and month must match the monthly fee");
            }
            if (monthlyFee.getTotalPayable() == null || monthlyFee.getPaidAmount() == null) {
                throw new IllegalArgumentException("Monthly fee balance is not configured");
            }

            if (monthlyFee.getStatus() == PaymentStatus.EXEMPT) {
                throw new IllegalArgumentException("This monthly fee is exempt from payment");
            }

            double recordedPaidAmount = getRecordedPaidAmount(monthlyFee.getId());
            double outstanding = monthlyFee.getTotalPayable() - recordedPaidAmount;
            if (outstanding <= 0 || payment.getAmountPaid() > outstanding) {
                throw new IllegalArgumentException("Payment amount exceeds the outstanding monthly fee balance");
            }
            monthlyFee.setPaidAmount(recordedPaidAmount);
        }

        Payment saved = paymentRepository.save(payment);
        if (monthlyFee != null) {
            refreshMonthlyFeeBalance(monthlyFee.getId());
        }
        return paymentConverter.entityToDTO(saved);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return paymentConverter.entityToDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByAdmissionNumber(Long admissionNumber) {
        return paymentRepository.findByAdmissionNumber(admissionNumber).stream()
                .map(paymentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PaymentDTO> getPaymentsByAdmissionNumber(Long admissionNumber, Pageable pageable) {
        return paymentRepository.findByAdmissionNumber(admissionNumber, pageable)
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
        if (status == PaymentStatus.PAID && payment.getStatus() != PaymentStatus.PAID) {
            MonthlyFee monthlyFee = monthlyFeeRepository.findById(payment.getMonthlyFeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "MonthlyFee", "id", payment.getMonthlyFeeId()));
            if (monthlyFee.getStatus() == PaymentStatus.EXEMPT
                    || monthlyFee.getTotalPayable() == null
                    || payment.getAmountPaid() == null
                    || payment.getAmountPaid() <= 0
                    || payment.getAmountPaid() > monthlyFee.getTotalPayable()
                            - getRecordedPaidAmount(monthlyFee.getId())) {
                throw new IllegalArgumentException("Payment amount exceeds the outstanding monthly fee balance");
            }
        }
        payment.setStatus(status);
        Payment saved = paymentRepository.save(payment);
        refreshMonthlyFeeBalance(payment.getMonthlyFeeId());
        return paymentConverter.entityToDTO(saved);
    }

    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        paymentRepository.delete(payment);
        refreshMonthlyFeeBalance(payment.getMonthlyFeeId());
    }

    private double getRecordedPaidAmount(Long monthlyFeeId) {
        double paidAmount = 0;
        for (Payment payment : paymentRepository.findByMonthlyFeeId(monthlyFeeId)) {
            if (payment.getStatus() == PaymentStatus.PAID && payment.getAmountPaid() != null) {
                paidAmount += payment.getAmountPaid();
            }
        }
        return paidAmount;
    }

    private void refreshMonthlyFeeBalance(Long monthlyFeeId) {
        MonthlyFee monthlyFee = monthlyFeeRepository.findById(monthlyFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyFee", "id", monthlyFeeId));
        if (monthlyFee.getStatus() == PaymentStatus.EXEMPT) {
            return;
        }
        if (monthlyFee.getTotalPayable() == null) {
            throw new IllegalArgumentException("Monthly fee balance is not configured");
        }

        double paidAmount = getRecordedPaidAmount(monthlyFeeId);
        monthlyFee.setPaidAmount(paidAmount);
        monthlyFee.setStatus(paidAmount >= monthlyFee.getTotalPayable()
                ? PaymentStatus.PAID
                : paidAmount > 0 ? PaymentStatus.PARTIAL : PaymentStatus.PENDING);
        monthlyFeeRepository.save(monthlyFee);
    }
}
