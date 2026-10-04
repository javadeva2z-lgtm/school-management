package com.school.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeeItemDTO {
    private Long id;

    @NotNull(message = "Service name is required")
    private String serviceName;

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotNull(message = "Mandatory flag is required")
    private Boolean mandatory;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount cannot be negative")
    private Double defaultAmount;

    private Boolean active;
}
