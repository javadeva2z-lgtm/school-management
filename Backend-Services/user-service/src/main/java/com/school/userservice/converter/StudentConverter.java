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
                .id(student.getId())
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
                .address(student.getAddress())
                .parentPhone(student.getParentPhone())
                .build();
    }

    public Student dtoToEntity(StudentDTO studentDTO) {
        if (studentDTO == null) {
            return null;
        }
        return Student.builder()
                .id(studentDTO.getId())
                .email(studentDTO.getEmail())
                .name(studentDTO.getName())
                .gender(studentDTO.getGender())
                .admissionNumber(studentDTO.getAdmissionNumber())
                .rollNumber(studentDTO.getRollNumber())
                .classId(studentDTO.getClassId())
                .sectionName(studentDTO.getSectionName())
                .fatherName(studentDTO.getFatherName())
                .motherName(studentDTO.getMotherName())
                .dateOfBirth(studentDTO.getDateOfBirth())
                .address(studentDTO.getAddress())
                .parentPhone(studentDTO.getParentPhone())
                .build();
    }
}
