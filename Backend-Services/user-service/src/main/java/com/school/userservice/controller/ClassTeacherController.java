package com.school.userservice.controller;

import com.school.userservice.dto.ClassTeacherDTO;
import com.school.userservice.service.ClassTeacherService;
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
@RequestMapping("/api/v1/class-teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Class Teachers", description = "Class teacher management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ClassTeacherController {
	private final ClassTeacherService classTeacherService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new class teacher")
	public ResponseEntity<ApiResponse<String>> createClassTeacher(@Valid @RequestBody ClassTeacherDTO classTeacherDto) {
		log.info("Create class teacher request received for teacher ID: {}", classTeacherDto.getTeacherId());
		classTeacherService.createClassTeacher(classTeacherDto);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Class teacher created successfully"));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
	@Operation(summary = "Get class teacher by ID")
	public ResponseEntity<ApiResponse<ClassTeacherDTO>> getClassTeacherById(@PathVariable Long id) {
		log.info("Get class teacher request received for id: {}", id);
		ClassTeacherDTO response = classTeacherService.getClassTeacherById(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
	
	@GetMapping("/teacher/{teacherId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
	@Operation(summary = "Get class teacher by teacher ID")
	public ResponseEntity<ApiResponse<ClassTeacherDTO>> getClassTeacherByTeacherId(@PathVariable Long teacherId) {
		log.info("Get class teacher request received for teacher id: {}", teacherId);
		ClassTeacherDTO response = classTeacherService.getClassTeacherByTeacherId(teacherId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/class/{classId}/section/{sectionName}")
	@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
	@Operation(summary = "Get class teacher by class ID and section name")
	public ResponseEntity<ApiResponse<ClassTeacherDTO>> getClassTeacherByClassAndSection(@PathVariable Long classId, @PathVariable String sectionName) {
		log.info("Get class teacher request received for class id: {} and section name: {}", classId, sectionName);
		ClassTeacherDTO response = classTeacherService.getClassTeacherByClassAndSection(classId, sectionName);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
	@Operation(summary = "Get all class teachers")
	public ResponseEntity<ApiResponse<List<ClassTeacherDTO>>> getAllClassTeachers() {
		log.info("Get all class teachers request received");
		List<ClassTeacherDTO> response = classTeacherService.getAllClassTeachers();
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
