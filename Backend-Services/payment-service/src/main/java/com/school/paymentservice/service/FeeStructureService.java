package com.school.paymentservice.service;

import com.school.paymentservice.dto.FeeStructureDTO;
import com.school.paymentservice.entity.FeeStructure;
import com.school.paymentservice.repository.FeeStructureRepository;
import com.school.paymentservice.converter.FeeStructureConverter;
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
public class FeeStructureService {
    private final FeeStructureRepository feeStructureRepository;
    private final FeeStructureConverter feeStructureConverter;

    public FeeStructureDTO createFeeStructure(FeeStructureDTO feeStructureDTO) {
        log.info("Creating fee structure for class: {} fee type: {}", feeStructureDTO.getClassId(), feeStructureDTO.getFeeType());
        
        if (feeStructureRepository.findByClassIdAndFeeTypeAndAcademicYear(
                feeStructureDTO.getClassId(), feeStructureDTO.getFeeType(), feeStructureDTO.getAcademicYear()).isPresent()) {
            throw new DuplicateResourceException("FeeStructure", "classId, feeType, academicYear", feeStructureDTO.getClassId());
        }

        FeeStructure feeStructure = feeStructureConverter.dtoToEntity(feeStructureDTO);
        feeStructure = feeStructureRepository.save(feeStructure);
        log.info("Fee structure created successfully with id: {}", feeStructure.getId());
        return feeStructureConverter.entityToDTO(feeStructure);
    }

    public FeeStructureDTO getFeeStructureById(Long id) {
        log.info("Fetching fee structure with id: {}", id);
        FeeStructure feeStructure = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", "id", id));
        return feeStructureConverter.entityToDTO(feeStructure);
    }

    public List<FeeStructureDTO> getFeeStructuresByClass(Long classId, String academicYear) {
        log.info("Fetching fee structures for class: {} academic year: {}", classId, academicYear);
        List<FeeStructure> feeStructures = feeStructureRepository.findByClassIdAndAcademicYear(classId, academicYear);
        return feeStructures.stream()
                .map(feeStructureConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<FeeStructureDTO> getAllActiveFeeStructures() {
        log.info("Fetching all active fee structures");
        List<FeeStructure> feeStructures = feeStructureRepository.findByIsActiveTrue();
        return feeStructures.stream()
                .map(feeStructureConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public FeeStructureDTO updateFeeStructure(Long id, FeeStructureDTO feeStructureDTO) {
        log.info("Updating fee structure with id: {}", id);
        FeeStructure feeStructure = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", "id", id));

        feeStructure.setAmount(feeStructureDTO.getAmount());
        feeStructure.setDueDate(feeStructureDTO.getDueDate());
        feeStructure.setIsActive(feeStructureDTO.getIsActive());

        feeStructure = feeStructureRepository.save(feeStructure);
        log.info("Fee structure updated successfully with id: {}", feeStructure.getId());
        return feeStructureConverter.entityToDTO(feeStructure);
    }

    public void deleteFeeStructure(Long id) {
        log.info("Deleting fee structure with id: {}", id);
        if (!feeStructureRepository.existsById(id)) {
            throw new ResourceNotFoundException("FeeStructure", "id", id);
        }
        feeStructureRepository.deleteById(id);
        log.info("Fee structure deleted successfully with id: {}", id);
    }
}
