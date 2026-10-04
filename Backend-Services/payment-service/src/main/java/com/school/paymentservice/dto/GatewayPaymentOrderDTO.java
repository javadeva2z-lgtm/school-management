package com.school.paymentservice.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GatewayPaymentOrderDTO {
    String reference;
    String provider;
    long admissionNumber;
    String status;
    long amountPaise;
    String currency;
    String qrImageUrl;
    String qrPayload;
    String providerReference;
    LocalDateTime expiresAt;
}
