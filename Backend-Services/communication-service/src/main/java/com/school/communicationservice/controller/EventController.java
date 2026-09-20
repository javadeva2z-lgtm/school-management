package com.school.communicationservice.controller;

import com.school.communicationservice.dto.EventDTO;
import com.school.communicationservice.service.EventService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EventController {
	private final EventService service;

	@PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
	public ResponseEntity<ApiResponse<EventDTO>> create(@Valid @RequestBody EventDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(service.create(dto), "Event created successfully"));
	}

	@GetMapping("/{id}")
	public ApiResponse<EventDTO> get(@PathVariable Long id) {
		return ApiResponse.success(service.get(id));
	}

	@GetMapping
	public ApiResponse<List<EventDTO>> list(@RequestParam(required = false) LocalDate from) {
		return ApiResponse.success(service.upcoming(from));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
	public ApiResponse<EventDTO> update(@PathVariable Long id, @Valid @RequestBody EventDTO dto) {
		return ApiResponse.success(service.update(id, dto), "Event updated successfully");
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ApiResponse.success(null, "Event deleted successfully");
	}
}
