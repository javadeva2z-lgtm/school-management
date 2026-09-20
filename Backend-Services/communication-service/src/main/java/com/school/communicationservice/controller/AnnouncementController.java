package com.school.communicationservice.controller;

import com.school.communicationservice.dto.AnnouncementDTO;
import com.school.communicationservice.service.AnnouncementService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "Announcement management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AnnouncementController {
	private final AnnouncementService service;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
	public ResponseEntity<ApiResponse<AnnouncementDTO>> create(@Valid @RequestBody AnnouncementDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(service.create(dto), "Announcement created successfully"));
	}

	@GetMapping("/{id}")
	public ApiResponse<AnnouncementDTO> get(@PathVariable Long id) {
		return ApiResponse.success(service.get(id));
	}

	@GetMapping
	public ApiResponse<List<AnnouncementDTO>> list(@RequestParam(required = false) Long classId) {
		return ApiResponse.success(service.active(classId));
	}

	@PutMapping("/{id}")
	public ApiResponse<AnnouncementDTO> update(@PathVariable Long id, @Valid @RequestBody AnnouncementDTO dto) {
		return ApiResponse.success(service.update(id, dto), "Announcement updated successfully");
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ApiResponse.success(null, "Announcement deleted successfully");
	}
}
