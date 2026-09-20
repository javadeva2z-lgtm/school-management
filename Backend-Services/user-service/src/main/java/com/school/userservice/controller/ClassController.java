package com.school.userservice.controller;

import com.school.userservice.dto.ClassDTO;
import com.school.userservice.service.ClassService;
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
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Classes", description = "Class management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ClassController {
    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new class")
    public ResponseEntity<ApiResponse<ClassDTO>> createClass(@Valid @RequestBody ClassDTO classDTO) {
        log.info("Create class request received for class name: {}", classDTO.getClassName());
        ClassDTO response = classService.createClass(classDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Class created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get class by ID")
    public ResponseEntity<ApiResponse<ClassDTO>> getClassById(@PathVariable Long id) {
        log.info("Get class request received for id: {}", id);
        ClassDTO response = classService.getClassById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update class")
    public ResponseEntity<ApiResponse<ClassDTO>> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody ClassDTO classDTO) {
        log.info("Update class request received for id: {}", id);
        ClassDTO response = classService.updateClass(id, classDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Class updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete class")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Long id) {
        log.info("Delete class request received for id: {}", id);
        classService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Class deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get all classes")
    public ResponseEntity<ApiResponse<List<ClassDTO>>> getAllClasses() {
        log.info("Get all classes request received");
        List<ClassDTO> response = classService.getAllClasses();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all active classes")
    public ResponseEntity<ApiResponse<List<ClassDTO>>> getAllActiveClasses() {
        log.info("Get all active classes request received");
        List<ClassDTO> response = classService.getAllActiveClasses();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
