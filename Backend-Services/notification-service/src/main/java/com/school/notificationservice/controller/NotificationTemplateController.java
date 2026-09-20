package com.school.notificationservice.controller;

import com.school.notificationservice.dto.NotificationTemplateDTO;
import com.school.notificationservice.service.NotificationTemplateService;
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
@RequestMapping("/api/v1/notification-templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Templates", description = "Notification template management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationTemplateController {
    private final NotificationTemplateService notificationTemplateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create notification template")
    public ResponseEntity<ApiResponse<NotificationTemplateDTO>> createTemplate(@Valid @RequestBody NotificationTemplateDTO templateDTO) {
        log.info("Create notification template request for template: {}", templateDTO.getTemplateName());
        NotificationTemplateDTO response = notificationTemplateService.createTemplate(templateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification template created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification template by ID")
    public ResponseEntity<ApiResponse<NotificationTemplateDTO>> getTemplateById(@PathVariable Long id) {
        log.info("Get notification template request for id: {}", id);
        NotificationTemplateDTO response = notificationTemplateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/name/{templateName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification template by name")
    public ResponseEntity<ApiResponse<NotificationTemplateDTO>> getTemplateByName(@PathVariable String templateName) {
        log.info("Get notification template request for name: {}", templateName);
        NotificationTemplateDTO response = notificationTemplateService.getTemplateByName(templateName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{notificationType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get templates by notification type")
    public ResponseEntity<ApiResponse<List<NotificationTemplateDTO>>> getTemplatesByType(
            @PathVariable String notificationType) {
        log.info("Get templates by type: {}", notificationType);
        List<NotificationTemplateDTO> response = notificationTemplateService.getTemplatesByType(notificationType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all active templates")
    public ResponseEntity<ApiResponse<List<NotificationTemplateDTO>>> getAllActiveTemplates() {
        log.info("Get all active notification templates request");
        List<NotificationTemplateDTO> response = notificationTemplateService.getAllActiveTemplates();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all templates")
    public ResponseEntity<ApiResponse<List<NotificationTemplateDTO>>> getAllTemplates() {
        log.info("Get all notification templates request");
        List<NotificationTemplateDTO> response = notificationTemplateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update notification template")
    public ResponseEntity<ApiResponse<NotificationTemplateDTO>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateDTO templateDTO) {
        log.info("Update notification template request for id: {}", id);
        NotificationTemplateDTO response = notificationTemplateService.updateTemplate(id, templateDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification template updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete notification template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("Delete notification template request for id: {}", id);
        notificationTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification template deleted successfully"));
    }
}
