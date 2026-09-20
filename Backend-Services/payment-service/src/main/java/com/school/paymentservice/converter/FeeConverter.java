package com.school.paymentservice.converter;

import com.school.paymentservice.dto.FeeDTO;
import com.school.paymentservice.entity.Fee;
import org.springframework.stereotype.Component;

@Component
public class FeeConverter {

    public FeeDTO entityToDTO(Fee fee) {
        if (fee == null) {
            return null;
        }
        return FeeDTO.builder()
                .id(fee.getId())
                .studentId(fee.getStudentId())
                .feeStructureId(fee.getFeeStructureId())
                .amount(fee.getAmount())
                .dueDate(fee.getDueDate())
                .academicYear(fee.getAcademicYear())
                .status(fee.getStatus())
                .build();
    }

    public Fee dtoToEntity(FeeDTO feeDTO) {
        if (feeDTO == null) {
            return null;
        }
        return Fee.builder()
                .id(feeDTO.getId())
                .studentId(feeDTO.getStudentId())
                .feeStructureId(feeDTO.getFeeStructureId())
                .amount(feeDTO.getAmount())
                .dueDate(feeDTO.getDueDate())
                .academicYear(feeDTO.getAcademicYear())
                .status(feeDTO.getStatus())
                .build();
    }
}
