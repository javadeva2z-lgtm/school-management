package com.school.paymentservice.controller;

import com.school.paymentservice.dto.FeeDTO;
import com.school.paymentservice.service.FeeService;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fees", description = "Fee management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FeeController {
    private final FeeService feeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create fee for student")
    public ResponseEntity<ApiResponse<FeeDTO>> createFee(@Valid @RequestBody FeeDTO feeDTO) {
        log.info("Create fee request received for student: {}", feeDTO.getStudentId());
        FeeDTO response = feeService.createFee(feeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Fee created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get fee by ID")
    public ResponseEntity<ApiResponse<FeeDTO>> getFeeById(@PathVariable Long id) {
        log.info("Get fee request received for id: {}", id);
        FeeDTO response = feeService.getFeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all fees for student")
    public ResponseEntity<ApiResponse<List<FeeDTO>>> getStudentFees(@PathVariable Long studentId) {
        log.info("Get student fees request received for student: {}", studentId);
        List<FeeDTO> response = feeService.getStudentFees(studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get paginated fees for student")
    public ResponseEntity<ApiResponse<Page<FeeDTO>>> getStudentFeesPaginated(
            @PathVariable Long studentId,
            Pageable pageable) {
        log.info("Get paginated student fees request for student: {}", studentId);
        Page<FeeDTO> response = feeService.getStudentFeesPaginated(studentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}/academic-year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get fees by academic year")
    public ResponseEntity<ApiResponse<List<FeeDTO>>> getStudentFeesByAcademicYear(
            @PathVariable Long studentId,
            @PathVariable String academicYear) {
        log.info("Get fees by academic year for student: {} year: {}", studentId, academicYear);
        List<FeeDTO> response = feeService.getStudentFeesByAcademicYear(studentId, academicYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all pending fees")
    public ResponseEntity<ApiResponse<List<FeeDTO>>> getPendingFees() {
        log.info("Get pending fees request received");
        List<FeeDTO> response = feeService.getPendingFees();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all overdue fees")
    public ResponseEntity<ApiResponse<List<FeeDTO>>> getOverdueFees() {
        log.info("Get overdue fees request received");
        List<FeeDTO> response = feeService.getOverdueFees();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update fee status")
    public ResponseEntity<ApiResponse<FeeDTO>> updateFeeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Update fee status request received for id: {} status: {}", id, status);
        FeeDTO response = feeService.updateFeeStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete fee")
    public ResponseEntity<ApiResponse<Void>> deleteFee(@PathVariable Long id) {
        log.info("Delete fee request received for id: {}", id);
        feeService.deleteFee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee deleted successfully"));
    }

    @GetMapping("/due-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get fees by due date range")
    public ResponseEntity<ApiResponse<List<FeeDTO>>> getFeesByDueDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Get fees by due date range from {} to {}", fromDate, toDate);
        List<FeeDTO> response = feeService.getFeesByDueDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
