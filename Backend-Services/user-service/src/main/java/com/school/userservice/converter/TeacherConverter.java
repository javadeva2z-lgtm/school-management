package com.school.userservice.converter;

import com.school.userservice.dto.TeacherDTO;
import com.school.userservice.entity.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherConverter {

    public TeacherDTO entityToDTO(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return TeacherDTO.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .gender(teacher.getGender())
                .username(teacher.getUsername())
                .employeeId(teacher.getEmployeeId())
                .qualification(teacher.getQualification())
                .specialization(teacher.getSpecialization())
                .joiningDate(teacher.getJoiningDate())
                .experienceYears(teacher.getExperienceYears())
                .email(teacher.getEmail())
                .address(teacher.getAddress())
                .phone(teacher.getPhone())
                .build();
    }

    public Teacher dtoToEntity(TeacherDTO teacherDTO) {
        if (teacherDTO == null) {
            return null;
        }
        return Teacher.builder()
                .id(teacherDTO.getId())
                .name(teacherDTO.getName())
                .gender(teacherDTO.getGender())
                .email(teacherDTO.getEmail())
                .username(teacherDTO.getUsername())
                .employeeId(teacherDTO.getEmployeeId())
                .qualification(teacherDTO.getQualification())
                .specialization(teacherDTO.getSpecialization())
                .joiningDate(teacherDTO.getJoiningDate())
                .experienceYears(teacherDTO.getExperienceYears())
                .address(teacherDTO.getAddress())
                .phone(teacherDTO.getPhone())
                .build();
    }
}
