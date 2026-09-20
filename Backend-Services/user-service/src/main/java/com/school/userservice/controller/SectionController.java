package com.school.userservice.controller;

import com.school.userservice.dto.SeatMatrixDTO;
import com.school.userservice.dto.SectionDTO;
import com.school.userservice.service.CommonService;
import com.school.userservice.service.SectionService;
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
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sections", description = "Section management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SectionController {
	private final SectionService sectionService;
	private final CommonService commonService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create a new section")
	public ResponseEntity<ApiResponse<SectionDTO>> createSection(@Valid @RequestBody SectionDTO sectionDTO) {
		log.info("Create section request received for section name: {}", sectionDTO.getSectionName());
		SectionDTO response = sectionService.createSection(sectionDTO);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(response, "Section created successfully"));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
	@Operation(summary = "Get section by ID")
	public ResponseEntity<ApiResponse<SectionDTO>> getSectionById(@PathVariable Long id) {
		log.info("Get section request received for id: {}", id);
		SectionDTO response = sectionService.getSectionById(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/class/{classId}")
	@Operation(summary = "Get all sections by class ID")
	public ResponseEntity<ApiResponse<List<SectionDTO>>> getSectionsByClassId(@PathVariable Long classId) {
		log.info("Get sections request received for class id: {}", classId);
		List<SectionDTO> response = sectionService.getSectionsByClassId(classId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/class/{classId}/active")
	@Operation(summary = "Get all active sections by class ID")
	public ResponseEntity<ApiResponse<List<SectionDTO>>> getActiveSectionsByClassId(@PathVariable Long classId) {
		log.info("Get active sections request received for class id: {}", classId);
		List<SectionDTO> response = sectionService.getActiveSectionsByClassId(classId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update section")
	public ResponseEntity<ApiResponse<SectionDTO>> updateSection(@PathVariable Long id,
			@Valid @RequestBody SectionDTO sectionDTO) {
		log.info("Update section request received for id: {}", id);
		SectionDTO response = sectionService.updateSection(id, sectionDTO);
		return ResponseEntity.ok(ApiResponse.success(response, "Section updated successfully"));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete section")
	public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long id) {
		log.info("Delete section request received for id: {}", id);
		sectionService.deleteSection(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Section deleted successfully"));
	}

	@GetMapping("/seat/matrix")
	@Operation(summary = "Get all sections seat matrix")
	public ResponseEntity<ApiResponse<List<SeatMatrixDTO>>> getAllSectionsSeats() {
		log.info("Get all sections seat matrix request received");
		List<SeatMatrixDTO> response = commonService.getAllSectionsSeatMatrix();
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	@Operation(summary = "Get all sections")
	public ResponseEntity<ApiResponse<List<SectionDTO>>> getAllSections() {
		log.info("Get all sections request received");
		List<SectionDTO> response = sectionService.getAllSections();
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
