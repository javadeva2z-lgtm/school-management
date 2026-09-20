package com.school.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Notification type is required")
    private String notificationType; // ATTENDANCE, HOMEWORK, RESULT, PAYMENT, EVENT, LEAVE

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private Long referenceId;

    private Boolean isRead;

    private LocalDateTime createdAt;
}
