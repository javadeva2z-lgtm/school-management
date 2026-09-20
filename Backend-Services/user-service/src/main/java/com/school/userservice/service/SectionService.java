package com.school.userservice.service;

import com.school.userservice.dto.SectionDTO;
import com.school.userservice.entity.Section;

import com.school.userservice.entity.Class;
import com.school.userservice.repository.ClassRepository;
import com.school.userservice.repository.SectionRepository;
import com.school.userservice.Constants;
import com.school.userservice.converter.SectionConverter;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SectionService {
    private final SectionRepository sectionRepository;
    private final SectionConverter sectionConverter;
    private final ClassRepository classRepository;

    public SectionDTO createSection(SectionDTO sectionDTO) {
        log.info("Creating section: {} for class: {}", sectionDTO.getSectionName(), sectionDTO.getClassId());
        
        if (sectionRepository.findByClassIdAndSectionName(sectionDTO.getClassId(), sectionDTO.getSectionName()).isPresent()) {
            throw new DuplicateResourceException("Section", "sectionName", sectionDTO.getSectionName());
        }

        Section section = sectionConverter.dtoToEntity(sectionDTO);
        section = sectionRepository.save(section);
        log.info("Section created successfully with id: {}", section.getId());
        return sectionConverter.entityToDTO(section);
    }

    public SectionDTO getSectionById(Long id) {
        log.info("Fetching section with id: {}", id);
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        return sectionConverter.entityToDTO(section);
    }

    public List<SectionDTO> getSectionsByClassId(Long classId) {
        log.info("Fetching sections for class: {}", classId);
        List<Section> sections = sectionRepository.findByClassId(classId);
        return sections.stream()
                .map(sectionConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<SectionDTO> getActiveSectionsByClassId(Long classId) {
        log.info("Fetching active sections for class: {}", classId);
        List<Section> sections = sectionRepository.findByClassIdAndIsActiveTrue(classId);
        return sections.stream()
                .map(sectionConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public SectionDTO updateSection(Long id, SectionDTO sectionDTO) {
        log.info("Updating section with id: {}", id);
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));

        section.setSectionName(sectionDTO.getSectionName());
        section.setCapacity(sectionDTO.getCapacity());
        section.setIsActive(sectionDTO.getIsActive());

        section = sectionRepository.save(section);
        log.info("Section updated successfully with id: {}", section.getId());
        return sectionConverter.entityToDTO(section);
    }

    public void deleteSection(Long id) {
        log.info("Deleting section with id: {}", id);
        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Section", "id", id);
        }
        sectionRepository.deleteById(id);
        log.info("Section deleted successfully with id: {}", id);
    }

    public List<SectionDTO> getAllSections() {
        log.info("Fetching all sections");
        List<Section> sections = sectionRepository.findAll();
        return sections.stream()
                .map(sectionConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    
 public void importCsv(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build();

            Iterable<CSVRecord> csvRecords = csvFormat.parse(fileReader);
            List<Section> sectionsToSave = new ArrayList<>();

            for (CSVRecord record : csvRecords) {
                Long classId = record.get(Constants.IMPORT_SECTION_COLUMN_CLASS_ID) != null ? Long.parseLong(record.get(Constants.IMPORT_SECTION_COLUMN_CLASS_ID)) : null;
                
                if (classId == null || classId <= 0) {
                    throw new IllegalArgumentException("CSV contains a record with a missing mandatory class ID.");
                }
                long id = record.isMapped(Constants.IMPORT_SECTION_COLUMN_ID) && !record.get(Constants.IMPORT_SECTION_COLUMN_ID).isBlank() ? Long.parseLong(record.get(Constants.IMPORT_SECTION_COLUMN_ID)) : 0;
                String className = record.get(Constants.IMPORT_SECTION_COLUMN_CLASS_NAME);
                String academicYear = record.get(Constants.IMPORT_SECTION_COLUMN_ACADEMIC_YEAR);
                
                int capacity =
                		record.isMapped(Constants.IMPORT_SECTION_COLUMN_CAPACITY) && !record.get(Constants.IMPORT_SECTION_COLUMN_CAPACITY).isBlank() ? Integer.parseInt(record.get(Constants.IMPORT_SECTION_COLUMN_CAPACITY)) : 0;
                String sectionName = record.get(Constants.IMPORT_SECTION_COLUMN_SECTION_NAME);
                Class classEntity = classRepository.findByClassId(classId).orElse(null);
                if (classEntity == null) {
                    //Create new class if not exists
                    classEntity = Class.builder()
                            .classId(classId)
                            .className(className)
                            .academicYear(academicYear)
                            .isActive(true)
                            .build();
                    classRepository.save(classEntity);
                }


                if(id > 0) {
                    Section existingSection = sectionRepository.findById(id).orElse(null);
                    if (existingSection != null) {
                       
                            existingSection.setClassId(classId);
                            existingSection.setCapacity(capacity);
                            existingSection.setSectionName(sectionName);
                            sectionsToSave.add(existingSection);
                            continue;
                    }
                }
                Section section = Section.builder()
                        .classId(classId)
                        .capacity(capacity)
                        .sectionName(sectionName)
                        .build();
                sectionsToSave.add(section);
            }
            sectionRepository.saveAll(sectionsToSave);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public void exportAllStudentsToCsv(java.io.Writer writer, Long classId, String sectionName) {
        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(
                Constants.IMPORT_SECTION_COLUMN_ID,
                Constants.IMPORT_SECTION_COLUMN_CLASS_ID,
                Constants.IMPORT_SECTION_COLUMN_CLASS_NAME,
                Constants.IMPORT_SECTION_COLUMN_SECTION_NAME,
                Constants.IMPORT_SECTION_COLUMN_CAPACITY,
                Constants.IMPORT_SECTION_COLUMN_ACADEMIC_YEAR
            ).build();

            List<Section> sections = sectionRepository.findAll();
            List<Class> classes = classRepository.findAll();
            Map<Long, Class> classMap = classes.stream().collect(Collectors.toMap(Class::getClassId, Function.identity()));
            csvFormat.print(writer).printRecords(sections.stream().map(s -> new Object[]{
                    s.getId(),
                    s.getClassId(),
                    classMap.getOrDefault(s.getClassId(), new Class()).getClassName(),
                    s.getSectionName(),
                    s.getCapacity(),
                    classMap.getOrDefault(s.getClassId(), new Class()).getAcademicYear()
            }).toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export sections to CSV: " + e.getMessage());
        }
    }
}
