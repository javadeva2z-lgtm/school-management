package com.school.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentServiceSubscriptionDTO {
    private Long id;
    private Long studentId;
    private Long feeItemId;
}