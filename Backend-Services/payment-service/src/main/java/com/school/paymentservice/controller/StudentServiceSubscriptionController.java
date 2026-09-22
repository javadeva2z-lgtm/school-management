package com.school.paymentservice.controller;

import com.school.common.response.ApiResponse;
import com.school.paymentservice.dto.StudentServiceSubscriptionDTO;
import com.school.paymentservice.service.StudentServiceSubscriptionService;
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
@RequestMapping("/api/v1/student-service-subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Student Service Subscriptions", description = "Student service subscription endpoints")
@SecurityRequirement(name = "bearerAuth")
public class StudentServiceSubscriptionController {
    private final StudentServiceSubscriptionService studentServiceSubscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create student service subscription")
    public ResponseEntity<ApiResponse<StudentServiceSubscriptionDTO>> createStudentServiceSubscription(
            @Valid @RequestBody StudentServiceSubscriptionDTO dto) {
        StudentServiceSubscriptionDTO response = studentServiceSubscriptionService
                .createStudentServiceSubscription(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student service subscription created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get subscription by id")
    public ResponseEntity<ApiResponse<StudentServiceSubscriptionDTO>> getById(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success(studentServiceSubscriptionService.getStudentServiceSubscriptionById(id)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get subscriptions by student")
    public ResponseEntity<ApiResponse<List<StudentServiceSubscriptionDTO>>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse
                .success(studentServiceSubscriptionService.getStudentServiceSubscriptionsByStudent(studentId)));
    }

    @GetMapping("/student/{studentId}/fee-item/{feeItemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get subscription by student and fee item")
    public ResponseEntity<ApiResponse<StudentServiceSubscriptionDTO>> getByStudentAndFeeItem(
            @PathVariable Long studentId,
            @PathVariable Long feeItemId) {
        return ResponseEntity.ok(ApiResponse.success(
                studentServiceSubscriptionService.getStudentServiceSubscription(studentId, feeItemId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete subscription")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        studentServiceSubscriptionService.deleteStudentServiceSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student service subscription deleted successfully"));
    }
}
