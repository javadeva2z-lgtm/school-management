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
import com.school.userservice.dto.ClassSubjectDTO;
import com.school.userservice.service.ClassSubjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/class-subjects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Class Subjects", description = "Class subject management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ClassSubjectController {
    private final ClassSubjectService classSubjectService;

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    @Operation(summary = "Get subjects for a class")
    public ResponseEntity<ApiResponse<List<ClassSubjectDTO>>> getSubjectsByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(ApiResponse.success(classSubjectService.getSubjectsByClass(classId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Add a subject to a class")
    public ResponseEntity<ApiResponse<ClassSubjectDTO>> createSubject(@Valid @RequestBody ClassSubjectDTO subjectDTO) {
        log.info("Create subject request received for class: {}", subjectDTO.getClassId());
        ClassSubjectDTO response = classSubjectService.createSubject(subjectDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Subject added to class successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update a class subject")
    public ResponseEntity<ApiResponse<ClassSubjectDTO>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody ClassSubjectDTO subjectDTO) {
        ClassSubjectDTO response = classSubjectService.updateSubject(id, subjectDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Class subject updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a class subject")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        classSubjectService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Class subject deleted successfully"));
    }
}
