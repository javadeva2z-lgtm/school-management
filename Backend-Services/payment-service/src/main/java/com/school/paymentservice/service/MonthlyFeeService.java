package com.school.paymentservice.service;

import com.school.common.enums.PaymentStatus;
import com.school.common.exception.ResourceNotFoundException;
import com.school.paymentservice.converter.MonthlyFeeConverter;
import com.school.paymentservice.client.StudentIdentity;
import com.school.paymentservice.client.StudentServiceClient;
import com.school.paymentservice.dto.MonthlyFeeDTO;
import com.school.paymentservice.entity.MonthlyFee;
import com.school.paymentservice.repository.FeeItemRepository;
import com.school.paymentservice.repository.MonthlyFeeRepository;
import com.school.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.time.YearMonth;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MonthlyFeeService {
    private final MonthlyFeeRepository monthlyFeeRepository;
    private final MonthlyFeeConverter monthlyFeeConverter;
    private final FeeItemRepository feeItemRepository;
    private final StudentServiceClient studentServiceClient;

    public MonthlyFeeDTO createMonthlyFee(MonthlyFeeDTO monthlyFeeDTO) {
        log.info("Creating monthly fee for admission number {} and month {}", monthlyFeeDTO.getAdmissionNumber(),
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
    public List<MonthlyFeeDTO> getMonthlyFeesByAdmissionNumber(Long admissionNumber) {
        return monthlyFeeRepository.findByAdmissionNumber(admissionNumber).stream()
                .map(monthlyFeeConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<MonthlyFeeDTO> generateMissingFees(Long admissionNumber, String authorization) {
        ApiResponse<StudentIdentity> studentResponse =
                studentServiceClient.getStudentByAdmissionNumber(admissionNumber, authorization);
        StudentIdentity student = studentResponse == null ? null : studentResponse.getData();
        if (student == null || !admissionNumber.equals(student.admissionNumber())
                || student.admissionDate() == null || student.classId() == null) {
            throw new IllegalArgumentException("Student admission date and class are required to calculate fees");
        }

        List<MonthlyFee> existingFees = monthlyFeeRepository.findByAdmissionNumber(admissionNumber);
        HashSet<String> existingMonths = existingFees.stream()
                .map(MonthlyFee::getMonthYear)
                .collect(Collectors.toCollection(HashSet::new));
        double monthlyAmount = feeItemRepository.findByClassIdAndActiveTrue(student.classId()).stream()
                .mapToDouble(feeItem -> feeItem.getDefaultAmount() == null ? 0 : feeItem.getDefaultAmount())
                .sum();
        if (monthlyAmount <= 0) {
            return existingFees.stream().map(monthlyFeeConverter::entityToDTO).toList();
        }

        YearMonth firstMonth = YearMonth.from(student.admissionDate());
        YearMonth currentMonth = YearMonth.now();
        List<MonthlyFee> newFees = new ArrayList<>();
        for (YearMonth month = firstMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            String monthYear = month.toString();
            if (!existingMonths.contains(monthYear)) {
                newFees.add(MonthlyFee.builder()
                        .admissionNumber(admissionNumber)
                        .monthYear(monthYear)
                        .baseAmount(monthlyAmount)
                        .waiverAmount(0.0)
                        .penaltyAmount(0.0)
                        .totalPayable(monthlyAmount)
                        .paidAmount(0.0)
                        .status(PaymentStatus.PENDING)
                        .build());
            }
        }
        monthlyFeeRepository.saveAll(newFees);
        return monthlyFeeRepository.findByAdmissionNumber(admissionNumber).stream()
                .map(monthlyFeeConverter::entityToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonthlyFeeDTO getMonthlyFeeByAdmissionNumberAndMonth(Long admissionNumber, String monthYear) {
        MonthlyFee monthlyFee = monthlyFeeRepository.findByAdmissionNumberAndMonthYear(admissionNumber, monthYear)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyFee", "admissionNumber + monthYear",
                        admissionNumber + "-" + monthYear));
        return monthlyFeeConverter.entityToDTO(monthlyFee);
    }

    public MonthlyFeeDTO updateMonthlyFee(Long id, MonthlyFeeDTO monthlyFeeDTO) {
        MonthlyFee existing = monthlyFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonthlyFee", "id", id));

        existing.setAdmissionNumber(monthlyFeeDTO.getAdmissionNumber());
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
