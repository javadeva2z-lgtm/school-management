package com.school.notificationservice.service;

import com.school.notificationservice.dto.NotificationTemplateDTO;
import com.school.notificationservice.entity.NotificationTemplate;
import com.school.notificationservice.repository.NotificationTemplateRepository;
import com.school.notificationservice.converter.NotificationTemplateConverter;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationTemplateService {
    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateConverter templateConverter;

    public NotificationTemplateDTO createTemplate(NotificationTemplateDTO templateDTO) {
        log.info("Creating notification template: {}", templateDTO.getTemplateName());
        
        if (templateRepository.findByTemplateName(templateDTO.getTemplateName()).isPresent()) {
            throw new DuplicateResourceException("NotificationTemplate", "templateName", templateDTO.getTemplateName());
        }

        NotificationTemplate template = templateConverter.dtoToEntity(templateDTO);
        template = templateRepository.save(template);
        log.info("Notification template created successfully with id: {}", template.getId());
        return templateConverter.entityToDTO(template);
    }

    public NotificationTemplateDTO getTemplateById(Long id) {
        log.info("Fetching notification template with id: {}", id);
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "id", id));
        return templateConverter.entityToDTO(template);
    }

    public NotificationTemplateDTO getTemplateByName(String templateName) {
        log.info("Fetching notification template with name: {}", templateName);
        NotificationTemplate template = templateRepository.findByTemplateName(templateName)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "templateName", templateName));
        return templateConverter.entityToDTO(template);
    }

    public List<NotificationTemplateDTO> getTemplatesByType(String notificationType) {
        log.info("Fetching notification templates by type: {}", notificationType);
        List<NotificationTemplate> templates = templateRepository.findByNotificationType(notificationType);
        return templates.stream()
                .map(templateConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationTemplateDTO> getAllActiveTemplates() {
        log.info("Fetching all active notification templates");
        List<NotificationTemplate> templates = templateRepository.findByIsActiveTrue();
        return templates.stream()
                .map(templateConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationTemplateDTO> getAllTemplates() {
        log.info("Fetching all notification templates");
        List<NotificationTemplate> templates = templateRepository.findAll();
        return templates.stream()
                .map(templateConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public NotificationTemplateDTO updateTemplate(Long id, NotificationTemplateDTO templateDTO) {
        log.info("Updating notification template with id: {}", id);
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "id", id));
        
        template.setSubject(templateDTO.getSubject());
        template.setMessageTemplate(templateDTO.getMessageTemplate());
        template.setIsActive(templateDTO.getIsActive());
        
        template = templateRepository.save(template);
        log.info("Notification template updated successfully with id: {}", template.getId());
        return templateConverter.entityToDTO(template);
    }

    public void deleteTemplate(Long id) {
        log.info("Deleting notification template with id: {}", id);
        if (!templateRepository.existsById(id)) {
            throw new ResourceNotFoundException("NotificationTemplate", "id", id);
        }
        templateRepository.deleteById(id);
        log.info("Notification template deleted successfully with id: {}", id);
    }

    public String processTemplate(String templateName, java.util.Map<String, String> placeholders) {
        log.info("Processing template: {}", templateName);
        NotificationTemplate template = templateRepository.findByTemplateName(templateName)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationTemplate", "templateName", templateName));
        
        String message = template.getMessageTemplate();
        for (java.util.Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
}
