package com.school.userservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.common.response.ApiResponse;
import com.school.userservice.dto.TeacherDTO;
import com.school.userservice.service.TeacherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teachers", description = "Teacher management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TeacherController {
    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new teacher")
    public ResponseEntity<ApiResponse<TeacherDTO>> createTeacher(@Valid @RequestBody TeacherDTO teacherDTO) {
        log.info("Create teacher request received for employee id: {}", teacherDTO.getEmployeeId());
        TeacherDTO response = teacherService.createTeacher(teacherDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Teacher created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get teacher by ID")
    public ResponseEntity<ApiResponse<TeacherDTO>> getTeacherById(@PathVariable Long id) {
        log.info("Get teacher request received for id: {}", id);
        TeacherDTO response = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get teacher by user ID")
    public ResponseEntity<ApiResponse<TeacherDTO>> getTeacherByUserId(@PathVariable String username) {
        log.info("Get teacher request received for username: {}", username);
        TeacherDTO response = teacherService.getTeacherByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Update teacher")
    public ResponseEntity<ApiResponse<TeacherDTO>> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherDTO teacherDTO) {
        log.info("Update teacher request received for id: {}", id);
        TeacherDTO response = teacherService.updateTeacher(id, teacherDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Teacher updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete teacher")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {
        log.info("Delete teacher request received for id: {}", id);
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Teacher deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all teachers")
    public ResponseEntity<ApiResponse<List<TeacherDTO>>> getAllTeachers() {
        log.info("Get all teachers request received");
        List<TeacherDTO> response = teacherService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
