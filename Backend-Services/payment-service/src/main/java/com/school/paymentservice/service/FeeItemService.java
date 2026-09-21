package com.school.paymentservice.service;

import com.school.common.exception.ResourceNotFoundException;
import com.school.paymentservice.converter.FeeItemConverter;
import com.school.paymentservice.dto.FeeItemDTO;
import com.school.paymentservice.entity.FeeItem;
import com.school.paymentservice.repository.FeeItemRepository;
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
public class FeeItemService {
    private final FeeItemRepository feeItemRepository;
    private final FeeItemConverter feeItemConverter;

    public FeeItemDTO createFeeItem(FeeItemDTO feeItemDTO) {
        log.info("Creating fee item for service: {}", feeItemDTO.getServiceName());
        FeeItem feeItem = feeItemConverter.dtoToEntity(feeItemDTO);
        FeeItem saved = feeItemRepository.save(feeItem);
        return feeItemConverter.entityToDTO(saved);
    }

    @Transactional(readOnly = true)
    public FeeItemDTO getFeeItemById(Long id) {
        FeeItem feeItem = feeItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeItem", "id", id));
        return feeItemConverter.entityToDTO(feeItem);
    }

    @Transactional(readOnly = true)
    public List<FeeItemDTO> getAllFeeItems() {
        return feeItemRepository.findAll().stream()
                .map(feeItemConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeeItemDTO> getActiveFeeItems() {
        return feeItemRepository.findByActiveTrue().stream()
                .map(feeItemConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeeItemDTO> getFeeItemsByClass(Long classId) {
        return feeItemRepository.findByClassIdAndActiveTrue(classId).stream()
                .map(feeItemConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeeItemDTO> getMandatoryFeeItemsByClass(Long classId, boolean mandatory) {
        return feeItemRepository.findByClassIdAndMandatoryAndActiveTrue(classId, mandatory).stream()
                .map(feeItemConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public FeeItemDTO updateFeeItem(Long id, FeeItemDTO feeItemDTO) {
        FeeItem existing = feeItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeItem", "id", id));

        existing.setClassId(feeItemDTO.getClassId());
        existing.setServiceName(feeItemDTO.getServiceName());
        existing.setMandatory(feeItemDTO.getMandatory());
        existing.setDefaultAmount(feeItemDTO.getDefaultAmount());
        existing.setActive(feeItemDTO.getActive() != null ? feeItemDTO.getActive() : true);

        return feeItemConverter.entityToDTO(feeItemRepository.save(existing));
    }

    public void deleteFeeItem(Long id) {
        if (!feeItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("FeeItem", "id", id);
        }
        feeItemRepository.deleteById(id);
    }
}
