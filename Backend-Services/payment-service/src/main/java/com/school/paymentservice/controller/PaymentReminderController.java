package com.school.paymentservice.controller;

import com.school.paymentservice.dto.PaymentReminderDTO;
import com.school.paymentservice.service.PaymentReminderService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-reminders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Reminders", description = "Payment reminder management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentReminderController {
    private final PaymentReminderService paymentReminderService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create payment reminder")
    public ResponseEntity<ApiResponse<PaymentReminderDTO>> createReminder(@Valid @RequestBody PaymentReminderDTO reminderDTO) {
        log.info("Create payment reminder request received for fee: {}", reminderDTO.getFeeId());
        PaymentReminderDTO response = paymentReminderService.createReminder(reminderDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment reminder created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get reminder by ID")
    public ResponseEntity<ApiResponse<PaymentReminderDTO>> getReminderById(@PathVariable Long id) {
        log.info("Get payment reminder request for id: {}", id);
        PaymentReminderDTO response = paymentReminderService.getReminderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get reminders for student")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getStudentReminders(@PathVariable Long studentId) {
        log.info("Get reminders for student: {}", studentId);
        List<PaymentReminderDTO> response = paymentReminderService.getStudentReminders(studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all pending reminders")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getPendingReminders() {
        log.info("Get pending reminders request received");
        List<PaymentReminderDTO> response = paymentReminderService.getPendingReminders();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date/{reminderDate}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get reminders by date")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getRemindersByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reminderDate) {
        log.info("Get reminders for date: {}", reminderDate);
        List<PaymentReminderDTO> response = paymentReminderService.getRemindersByDate(reminderDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/fee/{feeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get reminders for fee")
    public ResponseEntity<ApiResponse<List<PaymentReminderDTO>>> getFeeReminders(@PathVariable Long feeId) {
        log.info("Get reminders for fee: {}", feeId);
        List<PaymentReminderDTO> response = paymentReminderService.getFeeReminders(feeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/mark-sent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark reminder as sent")
    public ResponseEntity<ApiResponse<Void>> markReminderAsSent(@PathVariable Long id) {
        log.info("Mark reminder as sent request for id: {}", id);
        paymentReminderService.markReminderAsSent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reminder marked as sent"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete reminder")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable Long id) {
        log.info("Delete reminder request for id: {}", id);
        paymentReminderService.deleteReminder(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Reminder deleted successfully"));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate payment reminders")
    public ResponseEntity<ApiResponse<String>> generateReminders(
            @RequestParam(defaultValue = "3") int daysBeforeDue,
            @RequestParam(defaultValue = "7") int daysAfterDue) {
        log.info("Generate reminders request with daysBeforeDue: {} daysAfterDue: {}", daysBeforeDue, daysAfterDue);
        paymentReminderService.generateReminders(daysBeforeDue, daysAfterDue);
        return ResponseEntity.ok(ApiResponse.success("Reminders generated successfully", "Payment reminders generated"));
    }
}
