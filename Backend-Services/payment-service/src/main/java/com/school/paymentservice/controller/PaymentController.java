package com.school.paymentservice.controller;

import com.school.common.enums.PaymentStatus;
import com.school.common.response.ApiResponse;
import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;
    private final RestClient.Builder restClientBuilder;
    private final String userServiceBaseUrl;

    public PaymentController(
            PaymentService paymentService,
            RestClient.Builder restClientBuilder,
            @Value("${payment.gateways.user-service-base-url:http://localhost:8000/user-service}")
            String userServiceBaseUrl) {
        this.paymentService = paymentService;
        this.restClientBuilder = restClientBuilder;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Record payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            HttpServletRequest request) {
        Long studentId = resolveStudentId(paymentDTO.getAdmissionNumber(), request);
        PaymentDTO response = paymentService.createPayment(paymentDTO, studentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment recorded successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payment by id")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(id)));
    }

    @GetMapping("/admission/{admissionNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payments by student")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByAdmissionNumber(
            @PathVariable Long admissionNumber) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByAdmissionNumber(admissionNumber)));
    }

    @GetMapping("/admission/{admissionNumber}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get paginated payments by student")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByAdmissionNumberPaginated(
            @PathVariable Long admissionNumber,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentsByAdmissionNumber(admissionNumber, pageable)));
    }

    @GetMapping("/monthly-fee/{monthlyFeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payments for monthly fee")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByMonthlyFee(@PathVariable Long monthlyFeeId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByMonthlyFee(monthlyFeeId)));
    }

    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payment by transaction id")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByTransactionId(@PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByTransactionId(transactionId)));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payments by date range")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByDateRange(
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByDateRange(fromDate, toDate)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payments by status")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByStatus(status)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Update payment status")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePaymentStatus(@PathVariable Long id,
            @RequestParam PaymentStatus status) {
        PaymentDTO response = paymentService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete payment")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment deleted successfully"));
    }

    private Long resolveStudentId(Long admissionNumber, HttpServletRequest request) {
        if (admissionNumber == null || admissionNumber <= 0) {
            throw new IllegalArgumentException("A valid admission number is required");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            throw new IllegalStateException("Authenticated user token is required");
        }
        JsonNode response = restClientBuilder.build().get()
                .uri(userServiceBaseUrl + "/api/v1/students/admission/{admissionNumber}", admissionNumber)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(JsonNode.class);
        JsonNode student = response == null ? null : response.path("data");
        if (student == null || !student.path("id").isNumber()
                || !student.path("admissionNumber").canConvertToLong()
                || student.path("admissionNumber").asLong() != admissionNumber) {
            throw new IllegalArgumentException("Admission number does not identify a student");
        }
        return student.path("id").asLong();
    }
}
