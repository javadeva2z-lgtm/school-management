package com.school.notificationservice.controller;

import com.school.notificationservice.dto.SendNotificationDTO;
import com.school.notificationservice.dto.NotificationLogDTO;
import com.school.notificationservice.service.NotificationChannelService;
import com.school.notificationservice.service.NotificationLogService;
import com.school.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-channels")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Channels", description = "Multi-channel notification sending endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationChannelController {
    private final NotificationChannelService notificationChannelService;
    private final NotificationLogService notificationLogService;

    @PostMapping("/whatsapp")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send WhatsApp notification")
    public ResponseEntity<ApiResponse<String>> sendWhatsAppNotification(@Valid @RequestBody SendNotificationDTO sendNotificationDTO) {
        log.info("Send WhatsApp notification request for recipient: {}", sendNotificationDTO.getRecipientId());
        notificationChannelService.sendWhatsAppNotification(sendNotificationDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("WhatsApp notification queued", "Notification sent successfully"));
    }

    @PostMapping("/email")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send email notification")
    public ResponseEntity<ApiResponse<String>> sendEmailNotification(@Valid @RequestBody SendNotificationDTO sendNotificationDTO) {
        log.info("Send email notification request for recipient: {}", sendNotificationDTO.getRecipientId());
        notificationChannelService.sendEmailNotification(sendNotificationDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Email notification queued", "Notification sent successfully"));
    }

    @PostMapping("/sms")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send SMS notification")
    public ResponseEntity<ApiResponse<String>> sendSMSNotification(@Valid @RequestBody SendNotificationDTO sendNotificationDTO) {
        log.info("Send SMS notification request for recipient: {}", sendNotificationDTO.getRecipientId());
        notificationChannelService.sendSMSNotification(sendNotificationDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("SMS notification queued", "Notification sent successfully"));
    }

    @PostMapping("/push")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send push notification")
    public ResponseEntity<ApiResponse<String>> sendPushNotification(@Valid @RequestBody SendNotificationDTO sendNotificationDTO) {
        log.info("Send push notification request for recipient: {}", sendNotificationDTO.getRecipientId());
        notificationChannelService.sendPushNotification(sendNotificationDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Push notification queued", "Notification sent successfully"));
    }

    @GetMapping("/logs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification log by ID")
    public ResponseEntity<ApiResponse<NotificationLogDTO>> getLogById(@PathVariable Long id) {
        log.info("Get notification log request for id: {}", id);
        NotificationLogDTO response = notificationLogService.getLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/logs/recipient/{recipientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get notification logs for recipient")
    public ResponseEntity<ApiResponse<List<NotificationLogDTO>>> getLogsByRecipient(@PathVariable Long recipientId) {
        log.info("Get notification logs for recipient: {}", recipientId);
        List<NotificationLogDTO> response = notificationLogService.getLogsByRecipient(recipientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/logs/recipient/{recipientId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Get paginated notification logs")
    public ResponseEntity<ApiResponse<Page<NotificationLogDTO>>> getLogsByRecipientPaginated(
            @PathVariable Long recipientId,
            Pageable pageable) {
        log.info("Get paginated notification logs for recipient: {}", recipientId);
        Page<NotificationLogDTO> response = notificationLogService.getLogsByRecipientPaginated(recipientId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/logs/channel/{channel}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get logs by notification channel")
    public ResponseEntity<ApiResponse<List<NotificationLogDTO>>> getLogsByChannel(
            @PathVariable String channel) {
        log.info("Get logs by channel: {}", channel);
        List<NotificationLogDTO> response = notificationLogService.getLogsByChannel(channel);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/logs/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get logs by status")
    public ResponseEntity<ApiResponse<List<NotificationLogDTO>>> getLogsByStatus(
            @PathVariable String status) {
        log.info("Get logs by status: {}", status);
        List<NotificationLogDTO> response = notificationLogService.getLogsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/logs/failed-retry")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get failed logs for retry")
    public ResponseEntity<ApiResponse<List<NotificationLogDTO>>> getFailedLogsForRetry(
            @RequestParam(defaultValue = "3") Integer maxRetryCount) {
        log.info("Get failed logs for retry with max retry count: {}", maxRetryCount);
        List<NotificationLogDTO> response = notificationLogService.getFailedLogsForRetry(maxRetryCount);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/logs/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get logs by date range")
    public ResponseEntity<ApiResponse<List<NotificationLogDTO>>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        log.info("Get logs between {} and {}", fromDate, toDate);
        List<NotificationLogDTO> response = notificationLogService.getLogsByDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/logs/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update notification log status")
    public ResponseEntity<ApiResponse<Void>> updateLogStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Update log status for id: {} to status: {}", id, status);
        notificationLogService.updateLogStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(null, "Log status updated successfully"));
    }

    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retry failed notifications")
    public ResponseEntity<ApiResponse<String>> retryFailedNotifications() {
        log.info("Retry failed notifications request");
        notificationChannelService.retryFailedNotifications();
        return ResponseEntity.ok(ApiResponse.success("Retry process started", "Failed notifications will be retried"));
    }
}
