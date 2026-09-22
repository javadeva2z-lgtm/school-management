package com.school.paymentservice.converter;

import org.springframework.stereotype.Component;

import com.school.paymentservice.dto.PaymentReminderDTO;
import com.school.paymentservice.entity.PaymentReminder;

@Component
public class PaymentReminderConverter {

    public PaymentReminderDTO entityToDTO(PaymentReminder value) {
        if (value == null) {
            return null;
        }
        return PaymentReminderDTO.builder()
                .id(value.getId())
                .monthlyFeeId(value.getMonthlyFeeId())
                .studentId(value.getStudentId())
                .reminderType(value.getReminderType())
                .amount(value.getAmount())
                .dueDate(value.getDueDate())
                .sent(value.getSent())
                .sentAt(value.getSentAt())
                .build();
    }

    public PaymentReminder dtoToEntity(PaymentReminderDTO value) {
        if (value == null) {
            return null;
        }
        return PaymentReminder.builder()
                .id(value.getId())
                .monthlyFeeId(value.getMonthlyFeeId())
                .studentId(value.getStudentId())
                .reminderType(value.getReminderType())
                .amount(value.getAmount())
                .dueDate(value.getDueDate())
                .sent(value.getSent())
                .sentAt(value.getSentAt())
                .build();
    }
}
