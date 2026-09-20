package com.school.notificationservice.controller;

import com.school.notificationservice.dto.NotificationDTO;
import com.school.notificationservice.service.NotificationService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create notification")
    public ResponseEntity<ApiResponse<NotificationDTO>> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        log.info("Create notification request received for recipient: {}", notificationDTO.getRecipientId());
        NotificationDTO response = notificationService.createNotification(notificationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotificationById(@PathVariable Long id) {
        log.info("Get notification request for id: {}", id);
        NotificationDTO response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recipient/{recipientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get all notifications for recipient")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByRecipient(@PathVariable Long recipientId) {
        log.info("Get notifications request for recipient: {}", recipientId);
        List<NotificationDTO> response = notificationService.getNotificationsByRecipient(recipientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recipient/{recipientId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get paginated notifications for recipient")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotificationsByRecipientPaginated(
            @PathVariable Long recipientId,
            Pageable pageable) {
        log.info("Get paginated notifications for recipient: {}", recipientId);
        Page<NotificationDTO> response = notificationService.getNotificationsByRecipientPaginated(recipientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recipient/{recipientId}/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get unread notifications for recipient")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications(@PathVariable Long recipientId) {
        log.info("Get unread notifications for recipient: {}", recipientId);
        List<NotificationDTO> response = notificationService.getUnreadNotifications(recipientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recipient/{recipientId}/unread/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get paginated unread notifications")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getUnreadNotificationsPaginated(
            @PathVariable Long recipientId,
            Pageable pageable) {
        log.info("Get paginated unread notifications for recipient: {}", recipientId);
        Page<NotificationDTO> response = notificationService.getUnreadNotificationsPaginated(recipientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recipient/{recipientId}/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(@PathVariable Long recipientId) {
        log.info("Get unread notification count for recipient: {}", recipientId);
        Long response = notificationService.getUnreadNotificationCount(recipientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{notificationType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notifications by type")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByType(
            @PathVariable String notificationType) {
        log.info("Get notifications by type: {}", notificationType);
        List<NotificationDTO> response = notificationService.getNotificationsByType(notificationType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recipient/{recipientId}/type/{notificationType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get notifications by recipient and type")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByRecipientAndType(
            @PathVariable Long recipientId,
            @PathVariable String notificationType) {
        log.info("Get notifications for recipient: {} type: {}", recipientId, notificationType);
        List<NotificationDTO> response = notificationService.getNotificationsByRecipientAndType(recipientId, notificationType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        log.info("Mark notification as read request for id: {}", id);
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PutMapping("/recipient/{recipientId}/read-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long recipientId) {
        log.info("Mark all notifications as read request for recipient: {}", recipientId);
        notificationService.markAllAsRead(recipientId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        log.info("Delete notification request for id: {}", id);
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }

    @DeleteMapping("/recipient/{recipientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete all notifications for recipient")
    public ResponseEntity<ApiResponse<Void>> deleteNotificationsByRecipient(@PathVariable Long recipientId) {
        log.info("Delete all notifications for recipient: {}", recipientId);
        notificationService.deleteNotificationsByRecipient(recipientId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications deleted successfully"));
    }
}
