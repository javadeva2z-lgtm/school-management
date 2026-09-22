package com.school.paymentservice.converter;

import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentConverter {

    public PaymentDTO entityToDTO(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentDTO.builder()
                .id(payment.getId())
                .monthlyFeeId(payment.getMonthlyFeeId())
                .studentId(payment.getStudentId())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .monthYear(payment.getMonthYear())
                .build();
    }

    public Payment dtoToEntity(PaymentDTO payment) {
        if (payment == null) {
            return null;
        }
        return Payment.builder()
                .id(payment.getId())
                .monthlyFeeId(payment.getMonthlyFeeId())
                .studentId(payment.getStudentId())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .monthYear(payment.getMonthYear())
                .build();
    }
}
