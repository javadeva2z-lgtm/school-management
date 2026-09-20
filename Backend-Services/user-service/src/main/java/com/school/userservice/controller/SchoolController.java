package com.school.userservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.common.response.ApiResponse;
import com.school.userservice.dto.SchoolDTO;
import com.school.userservice.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schools", description = "School management endpoints")
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping("/public/all")
    @Operation(summary = "Get all schools")
    public ResponseEntity<ApiResponse<List<SchoolDTO>>> getAllSchools() {
        List<SchoolDTO> response = schoolService.getAllSchools();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Schools retrieved successfully"));
    }

    @GetMapping("/public/code/{schoolCode}")
    @Operation(summary = "Get school by code")
    public ResponseEntity<ApiResponse<SchoolDTO>> getSchoolByCode(@PathVariable String schoolCode) {
        SchoolDTO response = schoolService.getSchoolByCode(schoolCode);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, "School retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new school")
    public ResponseEntity<ApiResponse<SchoolDTO>> createSchool(@Valid @RequestBody SchoolDTO schoolDTO) {
        log.info("Create school request received for school name: {}", schoolDTO.getSchoolName());
        SchoolDTO response = schoolService.createSchool(schoolDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "School created successfully"));
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing school")
    public ResponseEntity<ApiResponse<SchoolDTO>> updateSchool(@Valid @RequestBody SchoolDTO schoolDTO) {
        log.info("Update school request received for school name: {}", schoolDTO.getSchoolName());
        SchoolDTO response = schoolService.updateSchool(schoolDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "School updated successfully"));
    }

    @PostMapping("/announcement")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update school announcement")
    public ResponseEntity<ApiResponse<SchoolDTO>> updateAnnouncement(@Valid @RequestBody SchoolDTO schoolDTO) {
        log.info("Update school announcement request received for school name: {}", schoolDTO.getSchoolName());
        SchoolDTO response = schoolService.updateSchoolAnnouncement(schoolDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response, "School announcement updated successfully"));
    }

}