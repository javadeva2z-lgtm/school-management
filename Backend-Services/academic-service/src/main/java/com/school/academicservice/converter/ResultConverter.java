package com.school.academicservice.converter;

import com.school.academicservice.dto.ResultDTO;
import com.school.academicservice.entity.Result;
import org.springframework.stereotype.Component;

@Component
public class ResultConverter {

    public ResultDTO entityToDTO(Result result) {
        if (result == null) {
            return null;
        }
        return ResultDTO.builder()
                .id(result.getId())
                .admissionNumber(result.getAdmissionNumber())
                .examScheduleId(result.getExamScheduleId())
                .subjectId(result.getSubjectId())
                .marksObtained(result.getMarksObtained())
                .outOf(result.getOutOf())
                .grade(result.getGrade())
                .fileUrl(result.getFileUrl())
                .build();
    }

    public Result dtoToEntity(ResultDTO resultDTO) {
        if (resultDTO == null) {
            return null;
        }
        return Result.builder()
                .id(resultDTO.getId())
                .admissionNumber(resultDTO.getAdmissionNumber())
                .examScheduleId(resultDTO.getExamScheduleId())
                .subjectId(resultDTO.getSubjectId())
                .marksObtained(resultDTO.getMarksObtained())
                .outOf(resultDTO.getOutOf())
                .grade(resultDTO.getGrade())
                .fileUrl(resultDTO.getFileUrl())
                .build();
    }
}
