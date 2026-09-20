package com.school.paymentservice.service;

import com.school.paymentservice.dto.FeeDTO;
import com.school.paymentservice.entity.Fee;
import com.school.paymentservice.repository.FeeRepository;
import com.school.paymentservice.converter.FeeConverter;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FeeService {
    private final FeeRepository feeRepository;
    private final FeeConverter feeConverter;

    public FeeDTO createFee(FeeDTO feeDTO) {
        log.info("Creating fee for student: {} fee structure: {}", feeDTO.getStudentId(), feeDTO.getFeeStructureId());
        
        if (feeRepository.findByStudentIdAndFeeStructureId(feeDTO.getStudentId(), feeDTO.getFeeStructureId()).isPresent()) {
            throw new DuplicateResourceException("Fee", "studentId and feeStructureId", feeDTO.getStudentId());
        }

        Fee fee = feeConverter.dtoToEntity(feeDTO);
        fee.setStatus("PENDING");
        fee = feeRepository.save(fee);
        log.info("Fee created successfully with id: {}", fee.getId());
        return feeConverter.entityToDTO(fee);
    }

    public FeeDTO getFeeById(Long id) {
        log.info("Fetching fee with id: {}", id);
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee", "id", id));
        return feeConverter.entityToDTO(fee);
    }

    public List<FeeDTO> getStudentFees(Long studentId) {
        log.info("Fetching fees for student: {}", studentId);
        List<Fee> fees = feeRepository.findByStudentId(studentId);
        return fees.stream()
                .map(feeConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public Page<FeeDTO> getStudentFeesPaginated(Long studentId, Pageable pageable) {
        log.info("Fetching paginated fees for student: {}", studentId);
        Page<Fee> fees = feeRepository.findByStudentId(studentId, pageable);
        return fees.map(feeConverter::entityToDTO);
    }

    public List<FeeDTO> getStudentFeesByAcademicYear(Long studentId, String academicYear) {
        log.info("Fetching fees for student: {} academic year: {}", studentId, academicYear);
        List<Fee> fees = feeRepository.findByStudentIdAndAcademicYear(studentId, academicYear);
        return fees.stream()
                .map(feeConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<FeeDTO> getPendingFees() {
        log.info("Fetching all pending fees");
        List<Fee> fees = feeRepository.findByStatus("PENDING");
        return fees.stream()
                .map(feeConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<FeeDTO> getOverdueFees() {
        log.info("Fetching all overdue fees");
        List<Fee> fees = feeRepository.findByStatus("OVERDUE");
        return fees.stream()
                .map(feeConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public FeeDTO updateFeeStatus(Long id, String status) {
        log.info("Updating fee status with id: {} to status: {}", id, status);
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee", "id", id));
        
        fee.setStatus(status);
        fee = feeRepository.save(fee);
        log.info("Fee status updated successfully with id: {}", fee.getId());
        return feeConverter.entityToDTO(fee);
    }

    public void deleteFee(Long id) {
        log.info("Deleting fee with id: {}", id);
        if (!feeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fee", "id", id);
        }
        feeRepository.deleteById(id);
        log.info("Fee deleted successfully with id: {}", id);
    }

    public List<FeeDTO> getFeesByDueDateRange(LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching fees due between {} and {}", fromDate, toDate);
        List<Fee> fees = feeRepository.findByDueDateBetween(fromDate, toDate);
        return fees.stream()
                .map(feeConverter::entityToDTO)
                .collect(Collectors.toList());
    }
}
