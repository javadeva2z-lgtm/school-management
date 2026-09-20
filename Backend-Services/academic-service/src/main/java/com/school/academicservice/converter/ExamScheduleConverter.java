package com.school.academicservice.converter;

import com.school.academicservice.dto.ExamScheduleDTO;
import com.school.academicservice.entity.ExamSchedule;
import org.springframework.stereotype.Component;

@Component
public class ExamScheduleConverter {

    public ExamScheduleDTO entityToDTO(ExamSchedule examSchedule) {
        if (examSchedule == null) {
            return null;
        }
        return ExamScheduleDTO.builder()
                .id(examSchedule.getId())
                .examName(examSchedule.getExamName())
                .classId(examSchedule.getClassId())
                .sectionName(examSchedule.getSectionName())
                .subjectId(examSchedule.getSubjectId())
                .examDate(examSchedule.getExamDate())
                .startTime(examSchedule.getStartTime())
                .endTime(examSchedule.getEndTime())
                .roomNumber(examSchedule.getRoomNumber())
                .maxMarks(examSchedule.getMaxMarks())
                .build();
    }

    public ExamSchedule dtoToEntity(ExamScheduleDTO examScheduleDTO) {
        if (examScheduleDTO == null) {
            return null;
        }
        return ExamSchedule.builder()
                .id(examScheduleDTO.getId())
                .examName(examScheduleDTO.getExamName())
                .classId(examScheduleDTO.getClassId())
                .sectionName(examScheduleDTO.getSectionName())
                .subjectId(examScheduleDTO.getSubjectId())
                .examDate(examScheduleDTO.getExamDate())
                .startTime(examScheduleDTO.getStartTime())
                .endTime(examScheduleDTO.getEndTime())
                .roomNumber(examScheduleDTO.getRoomNumber())
                .maxMarks(examScheduleDTO.getMaxMarks())
                .build();
    }
}
