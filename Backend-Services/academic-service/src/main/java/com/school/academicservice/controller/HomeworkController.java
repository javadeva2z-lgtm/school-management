package com.school.academicservice.controller;

import com.school.academicservice.dto.AttendanceDTO;
import com.school.academicservice.dto.HomeworkDTO;
import com.school.academicservice.dto.HomeworkFileDTO;
import com.school.academicservice.service.HomeworkService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/homework")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Homework", description = "Homework management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class HomeworkController {
    private final HomeworkService homeworkService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Assign new homework")
    public ResponseEntity<ApiResponse<HomeworkDTO>> createHomework(@Valid @RequestBody HomeworkDTO homeworkDTO) {
        log.info("Create homework request received for class: {}", homeworkDTO.getClassId());
        HomeworkDTO response = homeworkService.createHomework(homeworkDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Homework assigned successfully"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Assign new homework with files")
    public ResponseEntity<ApiResponse<HomeworkDTO>> createHomeworkWithFiles(
            @Valid @RequestPart("homework") HomeworkDTO homeworkDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        HomeworkDTO response = homeworkService.createHomework(homeworkDTO, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Homework assigned successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get homework by ID")
    public ResponseEntity<ApiResponse<HomeworkDTO>> getHomeworkById(@PathVariable Long id) {
        log.info("Get homework request received for id: {}", id);
        HomeworkDTO response = homeworkService.getHomeworkById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/class/{classId}/section/{sectionName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get homework by class and section")
    public ResponseEntity<ApiResponse<List<HomeworkDTO>>> getHomeworkByClassAndSection(
            @PathVariable Long classId,
            @PathVariable String sectionName) {
        log.info("Get homework request for class: {} section: {}", classId, sectionName);
        List<HomeworkDTO> response = homeworkService.getHomeworkByClassAndSection(classId, sectionName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/class/{classId}/section/{sectionName}/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get homework by class, section, and due date")
    public ResponseEntity<ApiResponse<List<HomeworkDTO>>> getHomeworkByClassSectionAndDueDate(
            @PathVariable Long classId,
            @PathVariable String sectionName,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Get homework request for class: {} section: {} date: {}", classId, sectionName, date);
        List<HomeworkDTO> response = homeworkService.byClassSectionAndDueDateBetween(classId, sectionName, date, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Get homework assigned by teacher")
    public ResponseEntity<ApiResponse<List<HomeworkDTO>>> getHomeworkByTeacher(@PathVariable Long teacherId) {
        log.info("Get homework request for teacher: {}", teacherId);
        List<HomeworkDTO> response = homeworkService.getHomeworkByTeacher(teacherId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get upcoming homework")
    public ResponseEntity<ApiResponse<List<HomeworkDTO>>> getUpcomingHomework(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Get upcoming homework request between {} and {}", fromDate, toDate);
        List<HomeworkDTO> response = homeworkService.getUpcomingHomework(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update homework")
    public ResponseEntity<ApiResponse<HomeworkDTO>> updateHomework(
            @PathVariable Long id,
            @Valid @RequestBody HomeworkDTO homeworkDTO) {
        log.info("Update homework request received for id: {}", id);
        HomeworkDTO response = homeworkService.updateHomework(id, homeworkDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Homework updated successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update homework with files")
    public ResponseEntity<ApiResponse<HomeworkDTO>> updateHomeworkWithFiles(
            @PathVariable Long id,
            @Valid @RequestPart("homework") HomeworkDTO homeworkDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        HomeworkDTO response = homeworkService.updateHomework(id, homeworkDTO, files);
        return ResponseEntity.ok(ApiResponse.success(response, "Homework updated successfully"));
    }

    @GetMapping("/files/{fileId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Download a homework file")
    public ResponseEntity<Resource> downloadHomeworkFile(@PathVariable Long fileId) throws IOException {
        HomeworkFileDTO file = homeworkService.getFile(fileId);
        Path filePath = Path.of(file.getFilePath());
        MediaType contentType = file.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.getContentType());

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(Files.size(filePath))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName(file.getFileName()) + "\"")
                .body(resource);
    }

    private String safeFileName(String fileName) {
        return fileName.replace("\"", "").replace("\r", "").replace("\n", "");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete homework")
    public ResponseEntity<ApiResponse<Void>> deleteHomework(@PathVariable Long id) {
        log.info("Delete homework request received for id: {}", id);
        homeworkService.deleteHomework(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Homework deleted successfully"));
    }
}
