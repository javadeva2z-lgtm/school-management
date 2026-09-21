package com.school.paymentservice.service;

import com.school.common.enums.PaymentStatus;
import com.school.common.exception.ResourceNotFoundException;
import com.school.paymentservice.converter.MonthlyFeeConverter;
import com.school.paymentservice.dto.MonthlyFeeDTO;
import com.school.paymentservice.entity.MonthlyFee;
import com.school.paymentservice.repository.MonthlyFeeRepository;
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
public class MonthlyFeeService {
    private final MonthlyFeeRepository monthlyFeeRepository;
    private final MonthlyFeeConverter monthlyFeeConverter;

    public MonthlyFeeDTO createMonthlyFee(MonthlyFeeDTO monthlyFeeDTO) {
        log.info("Creating monthly fee for student {} and month {}", monthlyFeeDTO.getStudentId(),
                monthlyFeeDTO.getMonthYear());
        MonthlyFee monthlyFee = monthlyFeeConverter.dtoToEntity(monthlyFeeDTO);
        MonthlyFee saved = monthlyFeeRepository.save(monthlyFee);
        return monthlyFeeConverter.entityToDTO(saved);
    }

    @Transactional(readOnly = true)
    public MonthlyFeeDTO getMonthlyFeeById(Long id) {
        MonthlyFee monthlyFee = monthlyFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyFee", "id", id));
        return monthlyFeeConverter.entityToDTO(monthlyFee);
    }

    @Transactional(readOnly = true)
    public List<MonthlyFeeDTO> getMonthlyFeesByStudent(Long studentId) {
        return monthlyFeeRepository.findByStudentId(studentId).stream()
                .map(monthlyFeeConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MonthlyFeeDTO getMonthlyFeeByStudentAndMonth(Long studentId, String monthYear) {
        MonthlyFee monthlyFee = monthlyFeeRepository.findByStudentIdAndMonthYear(studentId, monthYear)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyFee", "studentId + monthYear",
                        studentId + "-" + monthYear));
        return monthlyFeeConverter.entityToDTO(monthlyFee);
    }

    public MonthlyFeeDTO updateMonthlyFee(Long id, MonthlyFeeDTO monthlyFeeDTO) {
        MonthlyFee existing = monthlyFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyFee", "id", id));

        existing.setStudentId(monthlyFeeDTO.getStudentId());
        existing.setMonthYear(monthlyFeeDTO.getMonthYear());
        existing.setBaseAmount(monthlyFeeDTO.getBaseAmount());
        existing.setWaiverAmount(monthlyFeeDTO.getWaiverAmount());
        existing.setPenaltyAmount(monthlyFeeDTO.getPenaltyAmount());
        existing.setTotalPayable(monthlyFeeDTO.getTotalPayable());
        existing.setPaidAmount(monthlyFeeDTO.getPaidAmount());
        existing.setStatus(monthlyFeeDTO.getStatus() != null ? monthlyFeeDTO.getStatus() : PaymentStatus.PENDING);

        return monthlyFeeConverter.entityToDTO(monthlyFeeRepository.save(existing));
    }

    public MonthlyFeeDTO updateStatus(Long id, PaymentStatus status) {
        MonthlyFee monthlyFee = monthlyFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyFee", "id", id));
        monthlyFee.setStatus(status);
        return monthlyFeeConverter.entityToDTO(monthlyFeeRepository.save(monthlyFee));
    }

    public void deleteMonthlyFee(Long id) {
        if (!monthlyFeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("MonthlyFee", "id", id);
        }
        monthlyFeeRepository.deleteById(id);
    }
}
