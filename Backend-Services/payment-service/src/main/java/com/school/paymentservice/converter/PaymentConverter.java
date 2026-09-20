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
                .feeId(payment.getFeeId())
                .studentId(payment.getStudentId())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .receiptUrl(payment.getReceiptUrl())
                .status(payment.getStatus())
                .remarks(payment.getRemarks())
                .build();
    }

    public Payment dtoToEntity(PaymentDTO paymentDTO) {
        if (paymentDTO == null) {
            return null;
        }
        return Payment.builder()
                .id(paymentDTO.getId())
                .feeId(paymentDTO.getFeeId())
                .studentId(paymentDTO.getStudentId())
                .amountPaid(paymentDTO.getAmountPaid())
                .paymentMethod(paymentDTO.getPaymentMethod())
                .transactionId(paymentDTO.getTransactionId())
                .paymentDate(paymentDTO.getPaymentDate())
                .receiptUrl(paymentDTO.getReceiptUrl())
                .status(paymentDTO.getStatus())
                .remarks(paymentDTO.getRemarks())
                .build();
    }
}
