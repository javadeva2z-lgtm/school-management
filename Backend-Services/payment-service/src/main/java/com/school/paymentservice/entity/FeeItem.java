package com.school.paymentservice.entity;

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
import lombok.Builder.Default;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Data
@Table(name = "fee_items")
public class FeeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private Long classId;

    @Column(name = "service_name", nullable = false)
    private String serviceName; // "TUITION", "TRANSPORT", "AC_FEE", "EXTRA_ACTIVITY"

    @Column(name = "mandatory", nullable = false)
    private Boolean mandatory; // true for tuition/annual, false for optional services

    @Column(name = "default_amount", nullable = false)
    @Default
    private Double defaultAmount = 0.0;

    @Column(name = "active", nullable = false)
    @Default
    private Boolean active = true;

}
