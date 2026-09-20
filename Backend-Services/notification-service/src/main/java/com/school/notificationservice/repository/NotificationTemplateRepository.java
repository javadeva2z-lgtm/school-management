package com.school.notificationservice.repository;

import com.school.notificationservice.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByTemplateName(String templateName);
    List<NotificationTemplate> findByNotificationType(String notificationType);
    List<NotificationTemplate> findByIsActiveTrue();
}
