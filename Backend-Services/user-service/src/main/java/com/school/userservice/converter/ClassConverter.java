package com.school.userservice.converter;

import com.school.userservice.dto.ClassDTO;
import com.school.userservice.entity.Class;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassConverter {

    public ClassDTO entityToDTO(Class clazz) {
        if (clazz == null) {
            return null;
        }
        return ClassDTO.builder()
                .id(clazz.getId())
                .classId(clazz.getClassId())
                .className(clazz.getClassName())
                .academicYear(clazz.getAcademicYear())
                .isActive(clazz.getIsActive())
                .build();
    }

    public Class dtoToEntity(ClassDTO classDTO) {
        if (classDTO == null) {
            return null;
        }
        return Class.builder()
                .id(classDTO.getId())
                .classId(classDTO.getClassId())
                .className(classDTO.getClassName())
                .academicYear(classDTO.getAcademicYear())
                .isActive(classDTO.getIsActive())
                .build();
    }
}
