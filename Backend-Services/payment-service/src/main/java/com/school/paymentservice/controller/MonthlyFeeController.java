package com.school.paymentservice.controller;

import com.school.common.enums.PaymentStatus;
import com.school.common.response.ApiResponse;
import com.school.paymentservice.dto.MonthlyFeeDTO;
import com.school.paymentservice.service.MonthlyFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/monthly-fees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Monthly Fees", description = "Monthly fee management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MonthlyFeeController {
    private final MonthlyFeeService monthlyFeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create monthly fee")
    public ResponseEntity<ApiResponse<MonthlyFeeDTO>> createMonthlyFee(
            @Valid @RequestBody MonthlyFeeDTO monthlyFeeDTO) {
        MonthlyFeeDTO response = monthlyFeeService.createMonthlyFee(monthlyFeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Monthly fee created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get monthly fee by id")
    public ResponseEntity<ApiResponse<MonthlyFeeDTO>> getMonthlyFeeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(monthlyFeeService.getMonthlyFeeById(id)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get monthly fees by student")
    public ResponseEntity<ApiResponse<List<MonthlyFeeDTO>>> getMonthlyFeesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.success(monthlyFeeService.getMonthlyFeesByStudent(studentId)));
    }

    @GetMapping("/student/{studentId}/month/{monthYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get monthly fee by student and month")
    public ResponseEntity<ApiResponse<MonthlyFeeDTO>> getMonthlyFeeByStudentAndMonth(
            @PathVariable Long studentId,
            @PathVariable String monthYear) {
        return ResponseEntity
                .ok(ApiResponse.success(monthlyFeeService.getMonthlyFeeByStudentAndMonth(studentId, monthYear)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update monthly fee")
    public ResponseEntity<ApiResponse<MonthlyFeeDTO>> updateMonthlyFee(@PathVariable Long id,
            @Valid @RequestBody MonthlyFeeDTO monthlyFeeDTO) {
        MonthlyFeeDTO response = monthlyFeeService.updateMonthlyFee(id, monthlyFeeDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Monthly fee updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Update monthly fee status")
    public ResponseEntity<ApiResponse<MonthlyFeeDTO>> updateStatus(@PathVariable Long id,
            @RequestParam PaymentStatus status) {
        MonthlyFeeDTO response = monthlyFeeService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Monthly fee status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete monthly fee")
    public ResponseEntity<ApiResponse<Void>> deleteMonthlyFee(@PathVariable Long id) {
        monthlyFeeService.deleteMonthlyFee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Monthly fee deleted successfully"));
    }
}
