package com.school.academicservice.converter;

import com.school.academicservice.dto.HomeworkDTO;
import com.school.academicservice.dto.HomeworkFileDTO;
import com.school.academicservice.entity.Homework;
import org.springframework.stereotype.Component;

@Component
public class HomeworkConverter {

    public HomeworkDTO entityToDTO(Homework homework) {
        if (homework == null) {
            return null;
        }
        return HomeworkDTO.builder()
                .id(homework.getId())
                .teacherId(homework.getTeacherId())
                .classId(homework.getClassId())
                .sectionName(homework.getSectionName())
                .subjectId(homework.getSubjectId())
                .title(homework.getTitle())
                .description(homework.getDescription())
                .fileUrl(homework.getFileUrl())
                .dueDate(homework.getDueDate())
                .workType(homework.getWorkType())
                .files(homework.getFiles().stream()
                        .map(file -> HomeworkFileDTO.builder()
                                .id(file.getId())
                                .fileName(file.getFileName())
                                .contentType(file.getContentType())
                                .fileSize(file.getFileSize())
                                .filePath(file.getFilePath())
                                .downloadUrl(file.getDownloadUrl())
                                .build())
                        .toList())
                .build();
    }

    public Homework dtoToEntity(HomeworkDTO homeworkDTO) {
        if (homeworkDTO == null) {
            return null;
        }
        return Homework.builder()
                .id(homeworkDTO.getId())
                .teacherId(homeworkDTO.getTeacherId())
                .classId(homeworkDTO.getClassId())
                .sectionName(homeworkDTO.getSectionName())
                .subjectId(homeworkDTO.getSubjectId())
                .title(homeworkDTO.getTitle())
                .description(homeworkDTO.getDescription())
                .fileUrl(homeworkDTO.getFileUrl())
                .dueDate(homeworkDTO.getDueDate())
                .workType(homeworkDTO.getWorkType())
                .build();
    }
}
