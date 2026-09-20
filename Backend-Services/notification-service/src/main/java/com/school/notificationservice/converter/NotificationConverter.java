package com.school.notificationservice.converter;

import com.school.notificationservice.dto.NotificationDTO;
import com.school.notificationservice.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationConverter {

    public NotificationDTO entityToDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationDTO.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public Notification dtoToEntity(NotificationDTO notificationDTO) {
        if (notificationDTO == null) {
            return null;
        }
        return Notification.builder()
                .id(notificationDTO.getId())
                .recipientId(notificationDTO.getRecipientId())
                .notificationType(notificationDTO.getNotificationType())
                .title(notificationDTO.getTitle())
                .message(notificationDTO.getMessage())
                .referenceId(notificationDTO.getReferenceId())
                .isRead(notificationDTO.getIsRead())
                .build();
    }
}
