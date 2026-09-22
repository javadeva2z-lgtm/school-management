package com.school.paymentservice.controller;

import com.school.common.enums.PaymentStatus;
import com.school.common.response.ApiResponse;
import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Record payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        PaymentDTO response = paymentService.createPayment(paymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment recorded successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payment by id")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(id)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payments by student")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByStudent(studentId)));
    }

    @GetMapping("/student/{studentId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get paginated payments by student")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getPaymentsByStudentPaginated(
            @PathVariable Long studentId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsByStudent(studentId, pageable)));
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
}
