package com.school.paymentservice.converter;

import com.school.paymentservice.dto.MonthlyFeeDTO;
import com.school.paymentservice.entity.MonthlyFee;
import org.springframework.stereotype.Component;

@Component
public class MonthlyFeeConverter {

    public MonthlyFeeDTO entityToDTO(MonthlyFee studentFee) {
        if (studentFee == null) {
            return null;
        }
        return MonthlyFeeDTO.builder()
                .id(studentFee.getId())
                .studentId(studentFee.getStudentId())
                .monthYear(studentFee.getMonthYear())
                .baseAmount(studentFee.getBaseAmount())
                .waiverAmount(studentFee.getWaiverAmount())
                .penaltyAmount(studentFee.getPenaltyAmount())
                .totalPayable(studentFee.getTotalPayable())
                .paidAmount(studentFee.getPaidAmount())
                .status(studentFee.getStatus())
                .build();
    }

    public MonthlyFee dtoToEntity(MonthlyFeeDTO feeDTO) {
        if (feeDTO == null) {
            return null;
        }
        return MonthlyFee.builder()
                .id(feeDTO.getId())
                .studentId(feeDTO.getStudentId())
                .monthYear(feeDTO.getMonthYear())
                .baseAmount(feeDTO.getBaseAmount())
                .waiverAmount(feeDTO.getWaiverAmount())
                .penaltyAmount(feeDTO.getPenaltyAmount())
                .totalPayable(feeDTO.getTotalPayable())
                .paidAmount(feeDTO.getPaidAmount())
                .status(feeDTO.getStatus())
                .build();
    }
}
