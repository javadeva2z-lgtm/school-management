package com.school.notificationservice.repository;

import com.school.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientId(Long recipientId);
    Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);
    List<Notification> findByRecipientIdAndIsReadFalse(Long recipientId);
    Page<Notification> findByRecipientIdAndIsReadFalse(Long recipientId, Pageable pageable);
    List<Notification> findByNotificationType(String notificationType);
    List<Notification> findByRecipientIdAndNotificationType(Long recipientId, String notificationType);
    Long countByRecipientIdAndIsReadFalse(Long recipientId);
}
