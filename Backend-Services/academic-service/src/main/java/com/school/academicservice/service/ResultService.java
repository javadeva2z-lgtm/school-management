package com.school.academicservice.service;

import com.school.academicservice.dto.ResultDTO;
import com.school.academicservice.entity.Result;
import com.school.academicservice.repository.ResultRepository;
import com.school.academicservice.converter.ResultConverter;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ResultService {
    private final ResultRepository resultRepository;
    private final ResultConverter resultConverter;

    public ResultDTO publishResult(ResultDTO resultDTO) {
        log.info("Publishing result for student: {} exam: {}", resultDTO.getAdmissionNumber(), resultDTO.getExamScheduleId());
        
        if (resultRepository.findByAdmissionNumberAndExamScheduleIdAndSubjectId(resultDTO.getAdmissionNumber(), resultDTO.getExamScheduleId(), resultDTO.getSubjectId()).isPresent()) {
            throw new DuplicateResourceException("Result", "admissionNumber and examScheduleId and subjectId", resultDTO.getAdmissionNumber());
        }

        Result result = resultConverter.dtoToEntity(resultDTO);
        result.setPublishedDate(LocalDateTime.now());
        result = resultRepository.save(result);
        log.info("Result published successfully with id: {}", result.getId());
        return resultConverter.entityToDTO(result);
    }

    public ResultDTO getResultById(Long id) {
        log.info("Fetching result with id: {}", id);
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", id));
        return resultConverter.entityToDTO(result);
    }

    public List<ResultDTO> getResultByStudent(Long admissionNumber) {
        log.info("Fetching results for student: {}", admissionNumber);
        List<Result> results = resultRepository.findByAdmissionNumber(admissionNumber);
        return results.stream()
                .map(resultConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<ResultDTO> getResultByExamSchedule(Long examScheduleId) {
        log.info("Fetching results for exam schedule: {}", examScheduleId);
        List<Result> results = resultRepository.findByExamScheduleId(examScheduleId);
        return results.stream()
                .map(resultConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public ResultDTO updateResult(Long id, ResultDTO resultDTO) {
        log.info("Updating result with id: {}", id);
        Result result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result", "id", id));

        result.setMarksObtained(resultDTO.getMarksObtained());
        result.setOutOf(resultDTO.getOutOf());
        result.setGrade(resultDTO.getGrade());
        result.setFileUrl(resultDTO.getFileUrl());

        result = resultRepository.save(result);
        log.info("Result updated successfully with id: {}", result.getId());
        return resultConverter.entityToDTO(result);
    }

    public void deleteResult(Long id) {
        log.info("Deleting result with id: {}", id);
        if (!resultRepository.existsById(id)) {
            throw new ResourceNotFoundException("Result", "id", id);
        }
        resultRepository.deleteById(id);
        log.info("Result deleted successfully with id: {}", id);
    }

    // Helper method to calculate grade
    public String calculateGrade(Integer marksObtained, Integer totalMarks) {
        if (totalMarks == 0) return "F";
        
        double percentage = (double) marksObtained / totalMarks * 100;
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
}
