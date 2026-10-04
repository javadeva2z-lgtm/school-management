package com.school.paymentservice.service;

import com.school.common.exception.ResourceNotFoundException;
import com.school.paymentservice.converter.StudentServiceSubscriptionConverter;
import com.school.paymentservice.dto.StudentServiceSubscriptionDTO;
import com.school.paymentservice.entity.StudentServiceSubscription;
import com.school.paymentservice.repository.StudentServiceSubscriptionRepository;
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
public class StudentServiceSubscriptionService {
    private final StudentServiceSubscriptionRepository studentServiceSubscriptionRepository;
    private final StudentServiceSubscriptionConverter studentServiceSubscriptionConverter;

    public StudentServiceSubscriptionDTO createStudentServiceSubscription(StudentServiceSubscriptionDTO dto) {
        log.info("Creating service subscription: admissionNumber={}, feeItemId={}",
                dto.getAdmissionNumber(), dto.getFeeItemId());
        StudentServiceSubscription saved = studentServiceSubscriptionRepository.save(
                studentServiceSubscriptionConverter.dtoToEntity(dto));
        return studentServiceSubscriptionConverter.entityToDTO(saved);
    }

    @Transactional(readOnly = true)
    public StudentServiceSubscriptionDTO getStudentServiceSubscriptionById(Long id) {
        StudentServiceSubscription subscription = studentServiceSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentServiceSubscription", "id", id));
        return studentServiceSubscriptionConverter.entityToDTO(subscription);
    }

    @Transactional(readOnly = true)
    public List<StudentServiceSubscriptionDTO> getStudentServiceSubscriptionsByStudent(Long admissionNumber) {
        return studentServiceSubscriptionRepository.findByAdmissionNumber(admissionNumber).stream()
                .map(studentServiceSubscriptionConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentServiceSubscriptionDTO getStudentServiceSubscription(Long admissionNumber, Long feeItemId) {
        StudentServiceSubscription subscription = studentServiceSubscriptionRepository
                .findByAdmissionNumberAndFeeItemId(admissionNumber, feeItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "StudentServiceSubscription", "admissionNumber+feeItemId",
                        admissionNumber + "-" + feeItemId));
        return studentServiceSubscriptionConverter.entityToDTO(subscription);
    }

    public void deleteStudentServiceSubscription(Long id) {
        if (!studentServiceSubscriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("StudentServiceSubscription", "id", id);
        }
        studentServiceSubscriptionRepository.deleteById(id);
    }
}
