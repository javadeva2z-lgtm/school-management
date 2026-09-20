package com.school.notificationservice.repository;

import com.school.notificationservice.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByRecipientId(Long recipientId);
    Page<NotificationLog> findByRecipientId(Long recipientId, Pageable pageable);
    List<NotificationLog> findByNotificationChannel(String notificationChannel);
    List<NotificationLog> findByStatus(String status);
    List<NotificationLog> findByStatusAndRetryCountLessThan(String status, Integer retryCount);
    List<NotificationLog> findBySentAtBetween(LocalDateTime fromDate, LocalDateTime toDate);
    Long countByRecipientIdAndNotificationChannel(Long recipientId, String notificationChannel);
}
