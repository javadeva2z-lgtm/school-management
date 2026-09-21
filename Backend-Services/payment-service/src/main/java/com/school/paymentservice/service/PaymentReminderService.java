package com.school.paymentservice.service;

import com.school.common.exception.ResourceNotFoundException;
import com.school.paymentservice.converter.PaymentReminderConverter;
import com.school.paymentservice.dto.PaymentReminderDTO;
import com.school.paymentservice.entity.PaymentReminder;
import com.school.paymentservice.repository.PaymentReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentReminderService {
    private final PaymentReminderRepository paymentReminderRepository;
    private final PaymentReminderConverter paymentReminderConverter;

    public PaymentReminderDTO createReminder(PaymentReminderDTO reminderDTO) {
        log.info("Creating payment reminder for student {} and monthlyFee {}", reminderDTO.getStudentId(),
                reminderDTO.getMonthlyFeeId());
        PaymentReminder entity = paymentReminderConverter.dtoToEntity(reminderDTO);
        PaymentReminder saved = paymentReminderRepository.save(entity);
        return paymentReminderConverter.entityToDTO(saved);
    }

    @Transactional(readOnly = true)
    public PaymentReminderDTO getReminderById(Long id) {
        PaymentReminder reminder = paymentReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentReminder", "id", id));
        return paymentReminderConverter.entityToDTO(reminder);
    }

    @Transactional(readOnly = true)
    public List<PaymentReminderDTO> getRemindersByStudent(Long studentId) {
        return paymentReminderRepository.findByStudentId(studentId).stream()
                .map(paymentReminderConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentReminderDTO> getRemindersByMonthlyFee(Long monthlyFeeId) {
        return paymentReminderRepository.findByMonthlyFeeId(monthlyFeeId).stream()
                .map(paymentReminderConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentReminderDTO> getPendingRemindersByDate(LocalDate date) {
        return paymentReminderRepository.findByDueDateAndSentFalse(date).stream()
                .map(paymentReminderConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentReminderDTO> getPendingReminders() {
        return paymentReminderRepository.findBySentFalse().stream()
                .map(paymentReminderConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public PaymentReminderDTO markReminderAsSent(Long id) {
        PaymentReminder reminder = paymentReminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentReminder", "id", id));
        reminder.setSent(true);
        return paymentReminderConverter.entityToDTO(paymentReminderRepository.save(reminder));
    }

    public void deleteReminder(Long id) {
        if (!paymentReminderRepository.existsById(id)) {
            throw new ResourceNotFoundException("PaymentReminder", "id", id);
        }
        paymentReminderRepository.deleteById(id);
    }
}
