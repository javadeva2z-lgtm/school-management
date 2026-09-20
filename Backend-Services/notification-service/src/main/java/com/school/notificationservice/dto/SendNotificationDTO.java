package com.school.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendNotificationDTO {
    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotBlank(message = "Recipient phone/email is required")
    private String recipientAddress;

    @NotBlank(message = "Notification channel is required")
    private String notificationChannel; // WHATSAPP, EMAIL, SMS, PUSH

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String templateName; // Alternative to custom message
}
