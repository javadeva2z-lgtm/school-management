package com.school.paymentservice.controller;

import com.school.common.response.ApiResponse;
import com.school.paymentservice.dto.FeeItemDTO;
import com.school.paymentservice.service.FeeItemService;
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
@RequestMapping("/api/v1/fee-items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fee Items", description = "Fee item management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FeeItemController {
    private final FeeItemService feeItemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create fee item")
    public ResponseEntity<ApiResponse<FeeItemDTO>> createFeeItem(@Valid @RequestBody FeeItemDTO feeItemDTO) {
        log.info("Create fee item request for service: {}", feeItemDTO.getServiceName());
        FeeItemDTO response = feeItemService.createFeeItem(feeItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Fee item created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all fee items")
    public ResponseEntity<ApiResponse<List<FeeItemDTO>>> getAllFeeItems() {
        return ResponseEntity.ok(ApiResponse.success(feeItemService.getAllFeeItems()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get active fee items")
    public ResponseEntity<ApiResponse<List<FeeItemDTO>>> getActiveFeeItems() {
        return ResponseEntity.ok(ApiResponse.success(feeItemService.getActiveFeeItems()));
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get fee items by class")
    public ResponseEntity<ApiResponse<List<FeeItemDTO>>> getFeeItemsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(ApiResponse.success(feeItemService.getFeeItemsByClass(classId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get fee item by id")
    public ResponseEntity<ApiResponse<FeeItemDTO>> getFeeItemById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(feeItemService.getFeeItemById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update fee item")
    public ResponseEntity<ApiResponse<FeeItemDTO>> updateFeeItem(@PathVariable Long id,
            @Valid @RequestBody FeeItemDTO feeItemDTO) {
        FeeItemDTO response = feeItemService.updateFeeItem(id, feeItemDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Fee item updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete fee item")
    public ResponseEntity<ApiResponse<Void>> deleteFeeItem(@PathVariable Long id) {
        feeItemService.deleteFeeItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee item deleted successfully"));
    }
}
