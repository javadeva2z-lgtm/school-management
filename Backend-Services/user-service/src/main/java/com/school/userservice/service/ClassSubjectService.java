package com.school.userservice.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.school.common.exception.DuplicateResourceException;
import com.school.common.exception.ResourceNotFoundException;
import com.school.userservice.dto.ClassSubjectDTO;
import com.school.userservice.entity.ClassSubject;
import com.school.userservice.repository.ClassRepository;
import com.school.userservice.repository.ClassSubjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ClassSubjectService {
    private final ClassSubjectRepository classSubjectRepository;
    private final ClassRepository classRepository;

    public ClassSubjectDTO createSubject(ClassSubjectDTO subjectDTO) {
        requireClass(subjectDTO.getClassId());
        String subjectCode = subjectDTO.getSubjectCode().trim();
        if (classSubjectRepository.findByClassIdAndSubjectCode(subjectDTO.getClassId(), subjectCode).isPresent()) {
            throw new DuplicateResourceException("Subject", "subjectCode", subjectCode);
        }

        ClassSubject subject = ClassSubject.builder()
                .classId(subjectDTO.getClassId())
                .subjectName(subjectDTO.getSubjectName().trim())
                .subjectCode(subjectCode)
                .isActive(subjectDTO.getIsActive() == null || subjectDTO.getIsActive())
                .build();
        return toDTO(classSubjectRepository.save(subject));
    }

    @Transactional(readOnly = true)
    public List<ClassSubjectDTO> getSubjectsByClass(Long classId) {
        requireClass(classId);
        return classSubjectRepository.findByClassId(classId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassSubjectDTO> getAllSubjects() {
        return classSubjectRepository.findAll().stream().map(this::toDTO).toList();
    }

    public ClassSubjectDTO updateSubject(Long id, ClassSubjectDTO subjectDTO) {
        ClassSubject subject = classSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        requireClass(subjectDTO.getClassId());
        String subjectCode = subjectDTO.getSubjectCode().trim();
        if (classSubjectRepository.existsByClassIdAndSubjectCodeAndIdNot(subjectDTO.getClassId(), subjectCode, id)) {
            throw new DuplicateResourceException("Subject", "subjectCode", subjectCode);
        }

        subject.setClassId(subjectDTO.getClassId());
        subject.setSubjectName(subjectDTO.getSubjectName().trim());
        subject.setSubjectCode(subjectCode);
        subject.setIsActive(subjectDTO.getIsActive() == null || subjectDTO.getIsActive());
        return toDTO(classSubjectRepository.save(subject));
    }

    public void deleteSubject(Long id) {
        if (!classSubjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject", "id", id);
        }
        classSubjectRepository.deleteById(id);
        log.info("Deleted class subject with id: {}", id);
    }

    public void importCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build()
                    .parse(reader);
            try (parser) {
                if (!parser.getHeaderMap().keySet().containsAll(List.of("classId", "subjectName", "subjectCode"))) {
                    throw new IllegalArgumentException(
                            "CSV must include classId, subjectName, and subjectCode columns.");
                }

                for (CSVRecord record : parser) {
                    Long classId = Long.valueOf(requiredValue(record, "classId"));
                    String subjectName = requiredValue(record, "subjectName");
                    String subjectCode = requiredValue(record, "subjectCode");
                    requireClass(classId);

                    String rawId = optionalValue(record, "id");
                    ClassSubject subject;
                    if (rawId.isEmpty()) {
                        subject = classSubjectRepository.findByClassIdAndSubjectCode(classId, subjectCode).orElse(null);
                    } else {
                        Long id = Long.valueOf(rawId);
                        subject = classSubjectRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
                    }

                    if (subject == null) {
                        subject = ClassSubject.builder().build();
                    }
                    subject.setClassId(classId);
                    subject.setSubjectName(subjectName);
                    subject.setSubjectCode(subjectCode);
                    String activeValue = optionalValue(record, "isActive");
                    if (!activeValue.isEmpty()) {
                        subject.setIsActive(parseBoolean(activeValue));
                    } else if (subject.getIsActive() == null) {
                        subject.setIsActive(true);
                    }

                    Long subjectId = subject.getId();
                    boolean duplicate = classSubjectRepository.findByClassIdAndSubjectCode(classId, subjectCode)
                            .filter(existing -> !existing.getId().equals(subjectId))
                            .isPresent();
                    if (duplicate) {
                        throw new IllegalArgumentException("Duplicate subject code in class: " + subjectCode);
                    }
                    classSubjectRepository.save(subject);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse subject CSV file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public void exportSubjectsToCsv(Writer writer, Long classId) {
        List<ClassSubject> subjects = classId == null || classId == 0
                ? classSubjectRepository.findAll()
                : classSubjectRepository.findByClassId(classId);
        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("id", "classId", "subjectName", "subjectCode", "isActive")
                .build())) {
            for (ClassSubject subject : subjects) {
                printer.printRecord(
                        subject.getId(),
                        subject.getClassId(),
                        subject.getSubjectName(),
                        subject.getSubjectCode(),
                        subject.getIsActive());
            }
            printer.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export class subjects to CSV: " + e.getMessage(), e);
        }
    }

    private void requireClass(Long classId) {
        if (!classRepository.existsByClassId(classId)) {
            throw new ResourceNotFoundException("Class", "classId", classId);
        }
    }

    private String requiredValue(CSVRecord record, String column) {
        String value = optionalValue(record, column);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("CSV column '" + column + "' is required.");
        }
        return value;
    }

    private String optionalValue(CSVRecord record, String column) {
        if (!record.isMapped(column)) {
            return "";
        }
        return record.get(column).trim();
    }

    private boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            return false;
        }
        throw new IllegalArgumentException("CSV isActive must be true, false, 1, or 0.");
    }

    private ClassSubjectDTO toDTO(ClassSubject subject) {
        return ClassSubjectDTO.builder()
                .id(subject.getId())
                .classId(subject.getClassId())
                .subjectName(subject.getSubjectName())
                .subjectCode(subject.getSubjectCode())
                .isActive(subject.getIsActive())
                .build();
    }
}
