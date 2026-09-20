package com.school.userservice.service;

import com.school.userservice.dto.ClassDTO;
import com.school.userservice.entity.Class;
import com.school.userservice.repository.ClassRepository;
import com.school.userservice.converter.ClassConverter;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ClassService {
    private final ClassRepository classRepository;
    private final ClassConverter classConverter;

    public ClassDTO createClass(ClassDTO classDTO) {
        log.info("Creating class: {} for academic year: {}", classDTO.getClassName(), classDTO.getAcademicYear());
        
        if (classRepository.findByClassNameAndAcademicYear(classDTO.getClassName(), classDTO.getAcademicYear()).isPresent()) {
            throw new DuplicateResourceException("Class", "className and academicYear", classDTO.getClassName());
        }

        Class clazz = classConverter.dtoToEntity(classDTO);
        clazz = classRepository.save(clazz);
        log.info("Class created successfully with id: {}", clazz.getId());
        return classConverter.entityToDTO(clazz);
    }

    public ClassDTO getClassById(Long classId) {
        log.info("Fetching class with id: {}", classId);
        Class clazz = classRepository.findByClassId(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "classId", classId));
        return classConverter.entityToDTO(clazz);
    }

    public ClassDTO updateClass(Long classId, ClassDTO classDTO) {
        log.info("Updating class with id: {}", classId);
        Class clazz = classRepository.findByClassId(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "classId", classId));

        clazz.setClassName(classDTO.getClassName());
        clazz.setAcademicYear(classDTO.getAcademicYear());
        clazz.setIsActive(classDTO.getIsActive());

        clazz = classRepository.save(clazz);
        log.info("Class updated successfully with classId: {}", clazz.getClassId());
        return classConverter.entityToDTO(clazz);
    }

    public void deleteClass(Long classId) {
        log.info("Deleting class with classId: {}", classId);
        if (!classRepository.existsByClassId(classId)) {
            throw new ResourceNotFoundException("Class", "classId", classId);
        }
        classRepository.deleteByClassId(classId);
        log.info("Class deleted successfully with classId: {}", classId);
    }

    public List<ClassDTO> getAllClasses() {
        log.info("Fetching all classes");
        List<Class> classes = classRepository.findAll();
        return classes.stream()
                .map(classConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<ClassDTO> getAllActiveClasses() {
        log.info("Fetching all active classes");
        List<Class> classes = classRepository.findByIsActiveTrue();
        return classes.stream()
                .map(classConverter::entityToDTO)
                .collect(Collectors.toList());
    }
}
