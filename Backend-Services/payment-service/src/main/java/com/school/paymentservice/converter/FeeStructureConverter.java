package com.school.paymentservice.converter;

import com.school.paymentservice.dto.FeeStructureDTO;
import com.school.paymentservice.entity.FeeStructure;
import org.springframework.stereotype.Component;

@Component
public class FeeStructureConverter {

    public FeeStructureDTO entityToDTO(FeeStructure feeStructure) {
        if (feeStructure == null) {
            return null;
        }
        return FeeStructureDTO.builder()
                .id(feeStructure.getId())
                .classId(feeStructure.getClassId())
                .feeType(feeStructure.getFeeType())
                .amount(feeStructure.getAmount())
                .dueDate(feeStructure.getDueDate())
                .academicYear(feeStructure.getAcademicYear())
                .isActive(feeStructure.getIsActive())
                .build();
    }

    public FeeStructure dtoToEntity(FeeStructureDTO feeStructureDTO) {
        if (feeStructureDTO == null) {
            return null;
        }
        return FeeStructure.builder()
                .id(feeStructureDTO.getId())
                .classId(feeStructureDTO.getClassId())
                .feeType(feeStructureDTO.getFeeType())
                .amount(feeStructureDTO.getAmount())
                .dueDate(feeStructureDTO.getDueDate())
                .academicYear(feeStructureDTO.getAcademicYear())
                .isActive(feeStructureDTO.getIsActive())
                .build();
    }
}
