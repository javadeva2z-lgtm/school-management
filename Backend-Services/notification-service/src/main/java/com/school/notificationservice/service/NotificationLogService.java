package com.school.notificationservice.service;

import com.school.notificationservice.dto.NotificationLogDTO;
import com.school.notificationservice.entity.NotificationLog;
import com.school.notificationservice.repository.NotificationLogRepository;
import com.school.notificationservice.converter.NotificationLogConverter;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationLogService {
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationLogConverter notificationLogConverter;

    public NotificationLogDTO createLog(NotificationLogDTO logDTO) {
        log.info("Creating notification log for recipient: {} channel: {}", logDTO.getRecipientId(), logDTO.getNotificationChannel());
        
        NotificationLog notificationLog = notificationLogConverter.dtoToEntity(logDTO);
        notificationLog.setStatus("PENDING");
        notificationLog.setRetryCount(0);
        notificationLog = notificationLogRepository.save(notificationLog);
        log.info("Notification log created successfully with id: {}", notificationLog.getId());
        return notificationLogConverter.entityToDTO(notificationLog);
    }

    public NotificationLogDTO getLogById(Long id) {
        log.info("Fetching notification log with id: {}", id);
        NotificationLog notificationLog = notificationLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationLog", "id", id));
        return notificationLogConverter.entityToDTO(notificationLog);
    }

    public List<NotificationLogDTO> getLogsByRecipient(Long recipientId) {
        log.info("Fetching notification logs for recipient: {}", recipientId);
        List<NotificationLog> logs = notificationLogRepository.findByRecipientId(recipientId);
        return logs.stream()
                .map(notificationLogConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationLogDTO> getLogsByRecipientPaginated(Long recipientId, Pageable pageable) {
        log.info("Fetching paginated notification logs for recipient: {}", recipientId);
        Page<NotificationLog> logs = notificationLogRepository.findByRecipientId(recipientId, pageable);
        return logs.map(notificationLogConverter::entityToDTO);
    }

    public List<NotificationLogDTO> getLogsByChannel(String notificationChannel) {
        log.info("Fetching notification logs by channel: {}", notificationChannel);
        List<NotificationLog> logs = notificationLogRepository.findByNotificationChannel(notificationChannel);
        return logs.stream()
                .map(notificationLogConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationLogDTO> getLogsByStatus(String status) {
        log.info("Fetching notification logs by status: {}", status);
        List<NotificationLog> logs = notificationLogRepository.findByStatus(status);
        return logs.stream()
                .map(notificationLogConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationLogDTO> getFailedLogsForRetry(Integer maxRetryCount) {
        log.info("Fetching failed notification logs for retry with max retry count: {}", maxRetryCount);
        List<NotificationLog> logs = notificationLogRepository.findByStatusAndRetryCountLessThan("FAILED", maxRetryCount);
        return logs.stream()
                .map(notificationLogConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationLogDTO> getLogsByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Fetching notification logs between {} and {}", fromDate, toDate);
        List<NotificationLog> logs = notificationLogRepository.findBySentAtBetween(fromDate, toDate);
        return logs.stream()
                .map(notificationLogConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public void updateLogStatus(Long id, String status) {
        log.info("Updating notification log status with id: {} to status: {}", id, status);
        NotificationLog notificationLog = notificationLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationLog", "id", id));
        
        notificationLog.setStatus(status);
        if ("SENT".equals(status)) {
            notificationLog.setSentAt(LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            notificationLog.setDeliveredAt(LocalDateTime.now());
        }
        notificationLogRepository.save(notificationLog);
        log.info("Notification log status updated successfully with id: {}", id);
    }

    public void updateLogError(Long id, String errorMessage) {
        log.info("Updating notification log error with id: {}", id);
        NotificationLog notificationLog = notificationLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationLog", "id", id));
        
        notificationLog.setErrorMessage(errorMessage);
        notificationLog.setStatus("FAILED");
        notificationLog.setRetryCount(notificationLog.getRetryCount() + 1);
        notificationLogRepository.save(notificationLog);
        log.info("Notification log error updated successfully with id: {}", id);
    }

    public void deleteLog(Long id) {
        log.info("Deleting notification log with id: {}", id);
        if (!notificationLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("NotificationLog", "id", id);
        }
        notificationLogRepository.deleteById(id);
        log.info("Notification log deleted successfully with id: {}", id);
    }
}
