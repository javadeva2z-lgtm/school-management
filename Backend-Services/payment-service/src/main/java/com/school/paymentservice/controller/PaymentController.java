package com.school.paymentservice.controller;

import com.school.common.enums.PaymentStatus;
import com.school.common.response.ApiResponse;
import com.school.paymentservice.client.StudentIdentity;
import com.school.paymentservice.client.StudentServiceClient;
import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final StudentServiceClient studentServiceClient;

    public PaymentController(
            PaymentService paymentService,
            StudentServiceClient studentServiceClient) {
        this.paymentService = paymentService;
        this.studentServiceClient = studentServiceClient;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Record payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(
            @Valid @RequestBody PaymentDTO paymentDTO,
            HttpServletRequest request) {
        Long verifiedAdmissionNumber = resolveAdmissionNumber(
                paymentDTO.getAdmissionNumber(), request.getHeader(HttpHeaders.AUTHORIZATION));
        PaymentDTO response = paymentService.createPayment(paymentDTO, verifiedAdmissionNumber);
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

    private Long resolveAdmissionNumber(Long admissionNumber, String authorization) {
        if (admissionNumber == null || admissionNumber <= 0) {
            throw new IllegalArgumentException("A valid admission number is required");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        if (authorization == null || authorization.isBlank()) {
            throw new IllegalStateException("Authenticated user token is required");
        }
        ApiResponse<StudentIdentity> response =
                studentServiceClient.getStudentByAdmissionNumber(admissionNumber, authorization);
        StudentIdentity student = response == null ? null : response.getData();
        if (student == null || student.admissionNumber() == null
                || !student.admissionNumber().equals(admissionNumber)) {
            throw new IllegalArgumentException("Admission number does not identify a student");
        }
        return student.admissionNumber();
    }
}
