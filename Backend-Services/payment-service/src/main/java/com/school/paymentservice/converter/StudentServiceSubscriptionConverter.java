package com.school.paymentservice.converter;

import org.springframework.stereotype.Component;

import com.school.paymentservice.dto.StudentServiceSubscriptionDTO;
import com.school.paymentservice.entity.StudentServiceSubscription;

@Component
public class StudentServiceSubscriptionConverter {

    public StudentServiceSubscriptionDTO entityToDTO(StudentServiceSubscription value) {
        if (value == null) {
            return null;
        }
        return StudentServiceSubscriptionDTO.builder()
                .id(value.getId())
                .feeItemId(value.getFeeItemId())
                .studentId(value.getStudentId())
                .build();
    }

    public StudentServiceSubscription dtoToEntity(StudentServiceSubscriptionDTO value) {
        if (value == null) {
            return null;
        }
        return StudentServiceSubscription.builder()
                .id(value.getId())
                .feeItemId(value.getFeeItemId())
                .studentId(value.getStudentId())
                .build();
    }
}
