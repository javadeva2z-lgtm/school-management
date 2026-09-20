package com.school.notificationservice.service;

import com.school.notificationservice.dto.NotificationDTO;
import com.school.notificationservice.entity.Notification;
import com.school.notificationservice.repository.NotificationRepository;
import com.school.notificationservice.converter.NotificationConverter;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationConverter notificationConverter;

    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        log.info("Creating notification for recipient: {}", notificationDTO.getRecipientId());
        
        Notification notification = notificationConverter.dtoToEntity(notificationDTO);
        notification.setIsRead(false);
        notification = notificationRepository.save(notification);
        log.info("Notification created successfully with id: {}", notification.getId());
        return notificationConverter.entityToDTO(notification);
    }

    public NotificationDTO getNotificationById(Long id) {
        log.info("Fetching notification with id: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        return notificationConverter.entityToDTO(notification);
    }

    public List<NotificationDTO> getNotificationsByRecipient(Long recipientId) {
        log.info("Fetching notifications for recipient: {}", recipientId);
        List<Notification> notifications = notificationRepository.findByRecipientId(recipientId);
        return notifications.stream()
                .map(notificationConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationDTO> getNotificationsByRecipientPaginated(Long recipientId, Pageable pageable) {
        log.info("Fetching paginated notifications for recipient: {}", recipientId);
        Page<Notification> notifications = notificationRepository.findByRecipientId(recipientId, pageable);
        return notifications.map(notificationConverter::entityToDTO);
    }

    public List<NotificationDTO> getUnreadNotifications(Long recipientId) {
        log.info("Fetching unread notifications for recipient: {}", recipientId);
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalse(recipientId);
        return notifications.stream()
                .map(notificationConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationDTO> getUnreadNotificationsPaginated(Long recipientId, Pageable pageable) {
        log.info("Fetching paginated unread notifications for recipient: {}", recipientId);
        Page<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalse(recipientId, pageable);
        return notifications.map(notificationConverter::entityToDTO);
    }

    public Long getUnreadNotificationCount(Long recipientId) {
        log.info("Counting unread notifications for recipient: {}", recipientId);
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    public List<NotificationDTO> getNotificationsByType(String notificationType) {
        log.info("Fetching notifications by type: {}", notificationType);
        List<Notification> notifications = notificationRepository.findByNotificationType(notificationType);
        return notifications.stream()
                .map(notificationConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getNotificationsByRecipientAndType(Long recipientId, String notificationType) {
        log.info("Fetching notifications for recipient: {} type: {}", recipientId, notificationType);
        List<Notification> notifications = notificationRepository.findByRecipientIdAndNotificationType(recipientId, notificationType);
        return notifications.stream()
                .map(notificationConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long id) {
        log.info("Marking notification as read with id: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification marked as read with id: {}", id);
    }

    public void markAllAsRead(Long recipientId) {
        log.info("Marking all notifications as read for recipient: {}", recipientId);
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsReadFalse(recipientId);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("All notifications marked as read for recipient: {}", recipientId);
    }

    public void deleteNotification(Long id) {
        log.info("Deleting notification with id: {}", id);
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
        notificationRepository.deleteById(id);
        log.info("Notification deleted successfully with id: {}", id);
    }

    public void deleteNotificationsByRecipient(Long recipientId) {
        log.info("Deleting all notifications for recipient: {}", recipientId);
        List<Notification> notifications = notificationRepository.findByRecipientId(recipientId);
        notificationRepository.deleteAll(notifications);
        log.info("All notifications deleted for recipient: {}", recipientId);
    }
}
