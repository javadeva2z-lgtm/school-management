package com.school.utilityservice.controller;

import com.school.utilityservice.dto.FileMetadataDTO;
import com.school.utilityservice.service.FileStorageService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController @RequestMapping("/api/v1/files") @RequiredArgsConstructor
@Tag(name = "Files", description = "File storage management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    private final FileStorageService service;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileMetadataDTO>> upload(@RequestPart MultipartFile file, @RequestParam Long uploadedBy, @RequestParam(required = false) String entityType, @RequestParam(required = false) Long entityId) throws IOException { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.upload(file, uploadedBy, entityType, entityId), "File uploaded successfully")); }
    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<FileMetadataDTO>>> uploadBulk(@RequestPart List<MultipartFile> files, @RequestParam Long uploadedBy, @RequestParam(required = false) String entityType, @RequestParam(required = false) Long entityId) throws IOException { return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.uploadBulk(files, uploadedBy, entityType, entityId), "Files uploaded successfully")); }
    @GetMapping("/{id}") public ApiResponse<FileMetadataDTO> metadata(@PathVariable Long id) { return ApiResponse.success(service.metadata(id)); }
    @GetMapping("/uploaded-by/{uploadedBy}") public ApiResponse<List<FileMetadataDTO>> byUploader(@PathVariable Long uploadedBy) { return ApiResponse.success(service.byUploader(uploadedBy)); }
    @GetMapping("/{id}/download") public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) { FileMetadataDTO metadata = service.metadata(id); return ResponseEntity.ok().contentType(MediaType.parseMediaType(metadata.getFileType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : metadata.getFileType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"").body(new ByteArrayResource(service.download(id))); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) { service.delete(id); return ApiResponse.success(null, "File deleted successfully"); }
}
