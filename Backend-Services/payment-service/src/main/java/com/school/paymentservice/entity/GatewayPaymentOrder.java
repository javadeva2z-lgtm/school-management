package com.school.paymentservice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gateway_payment_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayPaymentOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", nullable = false, unique = true, length = 64)
    private String reference;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_reference", nullable = false, length = 128)
    private String providerReference;

    @Column(name = "admission_number", nullable = false)
    private Long admissionNumber;

    @Column(name = "monthly_fee_ids", nullable = false, length = 2000)
    private String monthlyFeeIds;

    @Column(name = "amount_paise", nullable = false)
    private Long amountPaise;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "qr_image_url", length = 1000)
    private String qrImageUrl;

    @Column(name = "qr_payload", length = 4000)
    private String qrPayload;

    @Column(name = "provider_payment_id", length = 128)
    private String providerPaymentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
