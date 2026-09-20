package com.school.notificationservice.converter;

import com.school.notificationservice.dto.NotificationLogDTO;
import com.school.notificationservice.entity.NotificationLog;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogConverter {

    public NotificationLogDTO entityToDTO(NotificationLog log) {
        if (log == null) {
            return null;
        }
        return NotificationLogDTO.builder()
                .id(log.getId())
                .recipientId(log.getRecipientId())
                .notificationChannel(log.getNotificationChannel())
                .recipientAddress(log.getRecipientAddress())
                .title(log.getTitle())
                .message(log.getMessage())
                .status(log.getStatus())
                .sentAt(log.getSentAt())
                .deliveredAt(log.getDeliveredAt())
                .errorMessage(log.getErrorMessage())
                .retryCount(log.getRetryCount())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public NotificationLog dtoToEntity(NotificationLogDTO logDTO) {
        if (logDTO == null) {
            return null;
        }
        return NotificationLog.builder()
                .id(logDTO.getId())
                .recipientId(logDTO.getRecipientId())
                .notificationChannel(logDTO.getNotificationChannel())
                .recipientAddress(logDTO.getRecipientAddress())
                .title(logDTO.getTitle())
                .message(logDTO.getMessage())
                .status(logDTO.getStatus())
                .sentAt(logDTO.getSentAt())
                .deliveredAt(logDTO.getDeliveredAt())
                .errorMessage(logDTO.getErrorMessage())
                .retryCount(logDTO.getRetryCount())
                .build();
    }
}
