package com.school.userservice.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.school.userservice.dto.StudentDTO;
import com.school.userservice.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Students", description = "Student management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {
    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Create a new student")
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        log.info("Create student request received for roll number: {}", studentDTO.getRollNumber());
        StudentDTO response = studentService.createStudent(studentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Student created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable Long id) {
        log.info("Get student request received for id: {}", id);
        StudentDTO response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/admission/{admissionNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get student by admission number")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentByAdmissionNumber(@PathVariable Long admissionNumber) {
        log.info("Get student request received for admission number: {}", admissionNumber);
        StudentDTO response = studentService.getStudentByUsernameOrAdmNumber(admissionNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/class/{classId}/section/{sectionName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all students by class and section")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getStudentsByClassAndSection(
            @PathVariable Long classId,
            @PathVariable String sectionName) {
        log.info("Get students request received for class: {} and section: {}", classId, sectionName);
        List<StudentDTO> response = studentService.getStudentsByClassAndSection(classId, sectionName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/class/{classId}/section/{sectionName}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get paginated students by class and section")
    public ResponseEntity<ApiResponse<Page<StudentDTO>>> getStudentsByClassAndSectionPaginated(
            @PathVariable Long classId,
            @PathVariable String sectionName,
            Pageable pageable) {
        log.info("Get paginated students request received for class: {} and section: {}", classId, sectionName);
        Page<StudentDTO> response = studentService.getStudentsByClassAndSection(classId, sectionName, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    @Operation(summary = "Update student")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO studentDTO) {
        log.info("Update student request received for id: {}", id);
        StudentDTO response = studentService.updateStudent(id, studentDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Student updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete student")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        log.info("Delete student request received for id: {}", id);
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all students")
    public ResponseEntity<ApiResponse<List<StudentDTO>>> getAllStudents() {
        log.info("Get all students request received");
        List<StudentDTO> response = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
