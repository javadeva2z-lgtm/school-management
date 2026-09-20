package com.school.paymentservice.controller;

import com.school.paymentservice.dto.FeeStructureDTO;
import com.school.paymentservice.service.FeeStructureService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fee-structure")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fee Structure", description = "Fee structure management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FeeStructureController {
    private final FeeStructureService feeStructureService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create fee structure")
    public ResponseEntity<ApiResponse<FeeStructureDTO>> createFeeStructure(@Valid @RequestBody FeeStructureDTO feeStructureDTO) {
        log.info("Create fee structure request received for class: {}", feeStructureDTO.getClassId());
        FeeStructureDTO response = feeStructureService.createFeeStructure(feeStructureDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Fee structure created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get fee structure by ID")
    public ResponseEntity<ApiResponse<FeeStructureDTO>> getFeeStructureById(@PathVariable Long id) {
        log.info("Get fee structure request received for id: {}", id);
        FeeStructureDTO response = feeStructureService.getFeeStructureById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get fee structures by class")
    public ResponseEntity<ApiResponse<List<FeeStructureDTO>>> getFeeStructuresByClass(
            @PathVariable Long classId,
            @RequestParam String academicYear) {
        log.info("Get fee structures request for class: {}", classId);
        List<FeeStructureDTO> response = feeStructureService.getFeeStructuresByClass(classId, academicYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all active fee structures")
    public ResponseEntity<ApiResponse<List<FeeStructureDTO>>> getAllActiveFeeStructures() {
        log.info("Get all active fee structures request received");
        List<FeeStructureDTO> response = feeStructureService.getAllActiveFeeStructures();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update fee structure")
    public ResponseEntity<ApiResponse<FeeStructureDTO>> updateFeeStructure(
            @PathVariable Long id,
            @Valid @RequestBody FeeStructureDTO feeStructureDTO) {
        log.info("Update fee structure request received for id: {}", id);
        FeeStructureDTO response = feeStructureService.updateFeeStructure(id, feeStructureDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee structure updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete fee structure")
    public ResponseEntity<ApiResponse<Void>> deleteFeeStructure(@PathVariable Long id) {
        log.info("Delete fee structure request received for id: {}", id);
        feeStructureService.deleteFeeStructure(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee structure deleted successfully"));
    }
}
