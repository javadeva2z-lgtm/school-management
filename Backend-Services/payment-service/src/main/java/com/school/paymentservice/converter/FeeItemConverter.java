package com.school.paymentservice.converter;

import com.school.paymentservice.dto.FeeItemDTO;
import com.school.paymentservice.entity.FeeItem;
import org.springframework.stereotype.Component;

@Component
public class FeeItemConverter {

    public FeeItemDTO entityToDTO(FeeItem feeType) {
        if (feeType == null) {
            return null;
        }
        return FeeItemDTO.builder()
                .id(feeType.getId())
                .serviceName(feeType.getServiceName())
                .classId(feeType.getClassId())
                .mandatory(feeType.getMandatory())
                .defaultAmount(feeType.getDefaultAmount())
                .active(feeType.getActive())
                .build();
    }

    public FeeItem dtoToEntity(FeeItemDTO feeType) {
        if (feeType == null) {
            return null;
        }
        return FeeItem.builder()
                .id(feeType.getId())
                .serviceName(feeType.getServiceName())
                .classId(feeType.getClassId())
                .mandatory(feeType.getMandatory())
                .defaultAmount(feeType.getDefaultAmount())
                .active(feeType.getActive())
                .build();
    }
}
