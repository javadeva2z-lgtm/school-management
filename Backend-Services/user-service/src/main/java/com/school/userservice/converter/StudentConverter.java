package com.school.userservice.converter;

import org.springframework.stereotype.Component;

import com.school.userservice.dto.StudentDTO;
import com.school.userservice.entity.Student;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StudentConverter {

    public StudentDTO entityToDTO(Student student) {
        if (student == null) {
            return null;
        }
        return StudentDTO.builder()
                .name(student.getName())
                .gender(student.getGender())
                .email(student.getEmail())
                .admissionNumber(student.getAdmissionNumber())
                .rollNumber(student.getRollNumber())
                .classId(student.getClassId())
                .sectionName(student.getSectionName())
                .fatherName(student.getFatherName())
                .motherName(student.getMotherName())
                .dateOfBirth(student.getDateOfBirth())
                .admissionDate(student.getAdmissionDate())
                .address(student.getAddress())
                .parentPhone(student.getParentPhone())
                .isEws(student.getIsEWS())
                .build();
    }

    public Student dtoToEntity(StudentDTO studentDTO) {
        if (studentDTO == null) {
            return null;
        }
        return Student.builder()
                .email(studentDTO.getEmail())
                .name(studentDTO.getName())
                .gender(studentDTO.getGender())
                .rollNumber(studentDTO.getRollNumber())
                .classId(studentDTO.getClassId())
                .sectionName(studentDTO.getSectionName())
                .fatherName(studentDTO.getFatherName())
                .motherName(studentDTO.getMotherName())
                .dateOfBirth(studentDTO.getDateOfBirth())
                .admissionDate(studentDTO.getAdmissionDate())
                .address(studentDTO.getAddress())
                .parentPhone(studentDTO.getParentPhone())
                .isEWS(studentDTO.getIsEws())
                .build();
    }
}
