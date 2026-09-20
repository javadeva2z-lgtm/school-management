package com.school.notificationservice.converter;

import com.school.notificationservice.dto.NotificationTemplateDTO;
import com.school.notificationservice.entity.NotificationTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateConverter {

    public NotificationTemplateDTO entityToDTO(NotificationTemplate template) {
        if (template == null) {
            return null;
        }
        return NotificationTemplateDTO.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .notificationType(template.getNotificationType())
                .subject(template.getSubject())
                .messageTemplate(template.getMessageTemplate())
                .isActive(template.getIsActive())
                .build();
    }

    public NotificationTemplate dtoToEntity(NotificationTemplateDTO templateDTO) {
        if (templateDTO == null) {
            return null;
        }
        return NotificationTemplate.builder()
                .id(templateDTO.getId())
                .templateName(templateDTO.getTemplateName())
                .notificationType(templateDTO.getNotificationType())
                .subject(templateDTO.getSubject())
                .messageTemplate(templateDTO.getMessageTemplate())
                .isActive(templateDTO.getIsActive())
                .build();
    }
}
