package com.school.paymentservice.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGatewayPaymentRequest {
    @NotBlank
    private String provider;

    @NotNull
    @Positive
    private Long studentId;

    @NotEmpty
    @Size(max = 50)
    private List<@NotNull @Positive Long> monthlyFeeIds;

    @NotBlank
    @Size(max = 100)
    private String customerName;

    @NotBlank
    @Email
    @Size(max = 254)
    private String customerEmail;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "\\+?[0-9 ()-]{10,20}")
    private String customerPhone;
}
