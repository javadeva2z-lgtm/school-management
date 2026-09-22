package com.school.paymentservice.controller;

import com.school.common.response.ApiResponse;
import com.school.paymentservice.dto.PaymentReminderDTO;
import com.school.paymentservice.service.PaymentReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-reminders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Reminders", description = "Payment reminder endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentReminderController {
    private final PaymentReminderService paymentReminderService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create payment reminder")
    public ResponseEntity<ApiResponse<PaymentReminderDTO>> createReminder(
            @Valid @RequestBody PaymentReminderDTO reminderDTO) {
        PaymentReminderDTO response = paymentReminderService.createReminder(reminderDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment reminder created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get reminder by id")
    public ResponseEntity<ApiResponse<PaymentReminderDTO>> getReminderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentReminderService.getReminderById(id)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get reminders by student")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getRemindersByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(paymentReminderService.getRemindersByStudent(studentId)));
    }

    @GetMapping("/monthly-fee/{monthlyFeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get reminders by monthly fee")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getRemindersByMonthlyFee(
            @PathVariable Long monthlyFeeId) {
        return ResponseEntity.ok(ApiResponse.success(paymentReminderService.getRemindersByMonthlyFee(monthlyFeeId)));
    }

    @GetMapping("/due-date")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get pending reminders by date")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getPendingRemindersByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(paymentReminderService.getPendingRemindersByDate(date)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all pending reminders")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getPendingReminders() {
        return ResponseEntity.ok(ApiResponse.success(paymentReminderService.getPendingReminders()));
    }

    @PatchMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark reminder as sent")
    public ResponseEntity<ApiResponse<PaymentReminderDTO>> markReminderAsSent(@PathVariable Long id) {
        PaymentReminderDTO response = paymentReminderService.markReminderAsSent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Reminder marked as sent"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete reminder")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable Long id) {
        paymentReminderService.deleteReminder(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reminder deleted successfully"));
    }
}
