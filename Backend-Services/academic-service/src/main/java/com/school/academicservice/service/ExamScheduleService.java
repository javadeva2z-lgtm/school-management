package com.school.academicservice.service;

import com.school.academicservice.dto.ExamScheduleDTO;
import com.school.academicservice.entity.ExamSchedule;
import com.school.academicservice.repository.ExamScheduleRepository;
import com.school.academicservice.converter.ExamScheduleConverter;
import com.school.common.exception.ResourceNotFoundException;
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
public class ExamScheduleService {
    private final ExamScheduleRepository examScheduleRepository;
    private final ExamScheduleConverter examScheduleConverter;

    public ExamScheduleDTO createExamSchedule(ExamScheduleDTO examScheduleDTO) {
        log.info("Creating exam schedule: {} for class: {} section: {}", examScheduleDTO.getExamName(), examScheduleDTO.getClassId(), examScheduleDTO.getSectionName());
        
        ExamSchedule examSchedule = examScheduleConverter.dtoToEntity(examScheduleDTO);
        examSchedule = examScheduleRepository.save(examSchedule);
        log.info("Exam schedule created successfully with id: {}", examSchedule.getId());
        return examScheduleConverter.entityToDTO(examSchedule);
    }

    public ExamScheduleDTO getExamScheduleById(Long id) {
        log.info("Fetching exam schedule with id: {}", id);
        ExamSchedule examSchedule = examScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamSchedule", "id", id));
        return examScheduleConverter.entityToDTO(examSchedule);
    }

    public List<ExamScheduleDTO> getExamScheduleByClassAndSection(Long classId, String sectionName) {
        log.info("Fetching exam schedule for class: {} section: {}", classId, sectionName);
        List<ExamSchedule> examSchedules = examScheduleRepository.findByClassIdAndSectionName(classId, sectionName);
        return examSchedules.stream()
                .map(examScheduleConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<ExamScheduleDTO> getUpcomingExams(LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching upcoming exams between {} and {}", fromDate, toDate);
        List<ExamSchedule> examSchedules = examScheduleRepository.findByExamDateBetween(fromDate, toDate);
        return examSchedules.stream()
                .map(examScheduleConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public ExamScheduleDTO updateExamSchedule(Long id, ExamScheduleDTO examScheduleDTO) {
        log.info("Updating exam schedule with id: {}", id);
        ExamSchedule examSchedule = examScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamSchedule", "id", id));

        examSchedule.setExamName(examScheduleDTO.getExamName());
        examSchedule.setExamDate(examScheduleDTO.getExamDate());
        examSchedule.setStartTime(examScheduleDTO.getStartTime());
        examSchedule.setEndTime(examScheduleDTO.getEndTime());
        examSchedule.setRoomNumber(examScheduleDTO.getRoomNumber());
        examSchedule.setMaxMarks(examScheduleDTO.getMaxMarks());

        examSchedule = examScheduleRepository.save(examSchedule);
        log.info("Exam schedule updated successfully with id: {}", examSchedule.getId());
        return examScheduleConverter.entityToDTO(examSchedule);
    }

    public void deleteExamSchedule(Long id) {
        log.info("Deleting exam schedule with id: {}", id);
        if (!examScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("ExamSchedule", "id", id);
        }
        examScheduleRepository.deleteById(id);
        log.info("Exam schedule deleted successfully with id: {}", id);
    }
}
