package com.school.paymentservice.controller;

import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.service.PaymentService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment recording and tracking endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Record payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> recordPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        log.info("Record payment request received for fee: {}", paymentDTO.getFeeId());
        PaymentDTO response = paymentService.recordPayment(paymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment recorded successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@PathVariable Long id) {
        log.info("Get payment request received for id: {}", id);
        PaymentDTO response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all payments for student")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getStudentPayments(@PathVariable Long studentId) {
        log.info("Get student payments request for student: {}", studentId);
        List<PaymentDTO> response = paymentService.getStudentPayments(studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get paginated payments for student")
    public ResponseEntity<ApiResponse<Page<PaymentDTO>>> getStudentPaymentsPaginated(
            @PathVariable Long studentId,
            Pageable pageable) {
        log.info("Get paginated student payments for student: {}", studentId);
        Page<PaymentDTO> response = paymentService.getStudentPaymentsPaginated(studentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/fee/{feeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all payments for fee")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getFeePayments(@PathVariable Long feeId) {
        log.info("Get fee payments request for fee: {}", feeId);
        List<PaymentDTO> response = paymentService.getFeePayments(feeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get payment by transaction ID")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByTransactionId(@PathVariable String transactionId) {
        log.info("Get payment request for transaction id: {}", transactionId);
        PaymentDTO response = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payments by date range")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        log.info("Get payments by date range from {} to {}", fromDate, toDate);
        List<PaymentDTO> response = paymentService.getPaymentsByDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        log.info("Update payment request received for id: {}", id);
        PaymentDTO response = paymentService.updatePayment(id, paymentDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete payment")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        log.info("Delete payment request received for id: {}", id);
        paymentService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment deleted successfully"));
    }

    @GetMapping("/student/{studentId}/total")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get total payments for student")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPaymentsByStudent(@PathVariable Long studentId) {
        log.info("Get total payments for student: {}", studentId);
        BigDecimal response = paymentService.getTotalPaymentsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
