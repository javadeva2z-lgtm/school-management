package com.school.paymentservice.service;

import com.school.paymentservice.dto.PaymentReminderDTO;
import com.school.paymentservice.entity.PaymentReminder;
import com.school.paymentservice.entity.Fee;
import com.school.paymentservice.repository.PaymentReminderRepository;
import com.school.paymentservice.repository.FeeRepository;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentReminderService {
    private final PaymentReminderRepository paymentReminderRepository;
    private final FeeRepository feeRepository;

    public PaymentReminderDTO createReminder(PaymentReminderDTO reminderDTO) {
        log.info("Creating payment reminder for fee: {}", reminderDTO.getFeeId());
        
        PaymentReminder reminder = PaymentReminder.builder()
                .feeId(reminderDTO.getFeeId())
                .studentId(reminderDTO.getStudentId())
                .reminderType(reminderDTO.getReminderType())
                .reminderDate(reminderDTO.getReminderDate())
                .isSent(false)
                .build();
        
        reminder = paymentReminderRepository.save(reminder);
        log.info("Payment reminder created successfully with id: {}", reminder.getId());
        return convertToDTO(reminder);
    }

    public PaymentReminderDTO getReminderById(Long id) {
        log.info("Fetching payment reminder with id: {}", id);
        PaymentReminder reminder = paymentReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentReminder", "id", id));
        return convertToDTO(reminder);
    }

    public List<PaymentReminderDTO> getStudentReminders(Long studentId) {
        log.info("Fetching reminders for student: {}", studentId);
        List<PaymentReminder> reminders = paymentReminderRepository.findByStudentId(studentId);
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentReminderDTO> getPendingReminders() {
        log.info("Fetching all pending reminders");
        List<PaymentReminder> reminders = paymentReminderRepository.findByIsSentFalse();
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentReminderDTO> getRemindersByDate(LocalDate reminderDate) {
        log.info("Fetching reminders for date: {}", reminderDate);
        List<PaymentReminder> reminders = paymentReminderRepository.findByReminderDateAndIsSentFalse(reminderDate);
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentReminderDTO> getFeeReminders(Long feeId) {
        log.info("Fetching reminders for fee: {}", feeId);
        List<PaymentReminder> reminders = paymentReminderRepository.findByFeeId(feeId);
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void markReminderAsSent(Long id) {
        log.info("Marking reminder as sent with id: {}", id);
        PaymentReminder reminder = paymentReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentReminder", "id", id));
        
        reminder.setIsSent(true);
        reminder.setSentAt(LocalDateTime.now());
        paymentReminderRepository.save(reminder);
        log.info("Reminder marked as sent with id: {}", id);
    }

    public void deleteReminder(Long id) {
        log.info("Deleting payment reminder with id: {}", id);
        if (!paymentReminderRepository.existsById(id)) {
            throw new ResourceNotFoundException("PaymentReminder", "id", id);
        }
        paymentReminderRepository.deleteById(id);
        log.info("Payment reminder deleted successfully with id: {}", id);
    }

    public void generateReminders(int daysBeforeDue, int daysAfterDue) {
        log.info("Generating payment reminders - days before due: {}, days after due: {}", daysBeforeDue, daysAfterDue);
        
        LocalDate today = LocalDate.now();
        LocalDate dueDateRange = today.plusDays(daysBeforeDue);
        LocalDate overdueRange = today.minusDays(daysAfterDue);
        
        List<Fee> upcomingFees = feeRepository.findByDueDateAndStatus(dueDateRange, "PENDING");
        List<Fee> overdueFees = feeRepository.findByDueDateBetween(overdueRange, today.minusDays(1));
        
        upcomingFees.forEach(fee -> {
            PaymentReminder reminder = PaymentReminder.builder()
                    .feeId(fee.getId())
                    .studentId(fee.getStudentId())
                    .reminderType("DUE_DATE")
                    .reminderDate(today)
                    .isSent(false)
                    .build();
            paymentReminderRepository.save(reminder);
        });
        
        overdueFees.forEach(fee -> {
            PaymentReminder reminder = PaymentReminder.builder()
                    .feeId(fee.getId())
                    .studentId(fee.getStudentId())
                    .reminderType("OVERDUE")
                    .reminderDate(today)
                    .isSent(false)
                    .build();
            paymentReminderRepository.save(reminder);
        });
        
        log.info("Generated {} upcoming fee reminders and {} overdue reminders", upcomingFees.size(), overdueFees.size());
    }

    private PaymentReminderDTO convertToDTO(PaymentReminder reminder) {
        Fee fee = feeRepository.findById(reminder.getFeeId()).orElse(null);
        return PaymentReminderDTO.builder()
                .id(reminder.getId())
                .feeId(reminder.getFeeId())
                .studentId(reminder.getStudentId())
                .reminderType(reminder.getReminderType())
                .reminderDate(reminder.getReminderDate())
                .isSent(reminder.getIsSent())
                .feeType(fee != null ? "Fee" : "N/A")
                .amount(fee != null ? fee.getAmount() : null)
                .dueDate(fee != null ? fee.getDueDate() : null)
                .build();
    }
}
