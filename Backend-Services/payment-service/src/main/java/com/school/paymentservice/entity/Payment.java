package com.school.paymentservice.entity;

import java.time.LocalDateTime;

import com.school.common.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_id", nullable = false)
	private Long studentId;

	@Column(name = "monthly_fee_id", nullable = false)
	private Long monthlyFeeId;

	@Column(name = "month", nullable = false)
	private String monthYear;

	@Column(name = "transaction_id", nullable = false)
	private String transactionId;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod; // "UPI", "CASH", "CREDIT_CARD", "NET_BANKING"

	@Column(name = "amount_paid", nullable = false)
	private Double amountPaid;

	@Column(name = "payment_date", nullable = false)
	private LocalDateTime paymentDate;

	@Column(name = "payment_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;
}
