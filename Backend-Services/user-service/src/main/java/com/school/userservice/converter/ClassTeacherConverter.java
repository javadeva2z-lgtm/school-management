package com.school.userservice.converter;

import com.school.userservice.dto.ClassTeacherDTO;
import com.school.userservice.entity.ClassTeacher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassTeacherConverter {

    public ClassTeacherDTO entityToDTO(ClassTeacher classTeacher) {
        if (classTeacher == null) {
            return null;
        }
        return ClassTeacherDTO.builder()
                .id(classTeacher.getId())
                .classId(classTeacher.getClassId())
                .sectionName(classTeacher.getSectionName())
                .teacherId(classTeacher.getTeacherId())
                .build();
    }

    public ClassTeacher dtoToEntity(ClassTeacherDTO classTeacherDTO) {
        if (classTeacherDTO == null) {
            return null;
        }
        return ClassTeacher.builder()
                .id(classTeacherDTO.getId())
                .classId(classTeacherDTO.getClassId())
                .sectionName(classTeacherDTO.getSectionName())
                .teacherId(classTeacherDTO.getTeacherId())
                .build();
    }
}
