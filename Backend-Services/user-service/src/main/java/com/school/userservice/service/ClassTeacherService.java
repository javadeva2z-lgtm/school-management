package com.school.userservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.school.common.exception.ResourceNotFoundException;
import com.school.userservice.converter.ClassTeacherConverter;
import com.school.userservice.dto.ClassTeacherDTO;
import com.school.userservice.entity.ClassTeacher;
import com.school.userservice.repository.ClassTeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ClassTeacherService {
    private final ClassTeacherRepository classTeacherRepository;
    private final ClassTeacherConverter classTeacherConverter;

    public void createClassTeacher(ClassTeacherDTO classTeacherDTO) {
        log.info("Creating class teacher with class id: {}", classTeacherDTO.getClassId());
        ClassTeacher existingClassTeacher = classTeacherRepository.findByClassIdAndSectionName(classTeacherDTO.getClassId(), classTeacherDTO.getSectionName())
                .orElse(null);
        if (existingClassTeacher != null && !existingClassTeacher.getTeacherId().equals(classTeacherDTO.getTeacherId())) {
           existingClassTeacher.setTeacherId(classTeacherDTO.getTeacherId());
            classTeacherRepository.save(existingClassTeacher);
            log.info("Updated existing class teacher with id: {}", existingClassTeacher.getId());
           }else {
            ClassTeacher classTeacher = classTeacherConverter.dtoToEntity(classTeacherDTO);
            classTeacher = classTeacherRepository.save(classTeacher);
            log.info("Class teacher created successfully with id: {}", classTeacher.getId());
        }
    }

    public ClassTeacherDTO getClassTeacherById(Long id) {
        log.info("Fetching class teacher with id: {}", id);
        ClassTeacher classTeacher = classTeacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class Teacher", "id", id));
        return classTeacherConverter.entityToDTO(classTeacher);
    }
        public ClassTeacherDTO getClassTeacherByTeacherId(Long teacherId) {
        log.info("Fetching class teacher with teacher id: {}", teacherId);
        ClassTeacher classTeacher = classTeacherRepository.findByTeacherId(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Class Teacher", "teacherId", teacherId));
        return classTeacherConverter.entityToDTO(classTeacher);
    }

    public ClassTeacherDTO getClassTeacherByClassAndSection(Long classId, String sectionName) {
        log.info("Fetching class teacher with class id: {} and section name: {}", classId, sectionName);
        ClassTeacher classTeacher = classTeacherRepository.findByClassIdAndSectionName(classId, sectionName)
                .orElseThrow(() -> new ResourceNotFoundException("Class Teacher", "classId and sectionName", classId + " and " + sectionName));
        return classTeacherConverter.entityToDTO(classTeacher);
    }

    public List<ClassTeacherDTO> getAllClassTeachers() {
        log.info("Fetching all class teachers");
        List<ClassTeacher> classTeachers = classTeacherRepository.findAll();
        return classTeachers.stream()
                .map(classTeacherConverter::entityToDTO)
                .collect(Collectors.toList());
    }
}