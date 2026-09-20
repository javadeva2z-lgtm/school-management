package com.school.academicservice.service;

import com.school.academicservice.converter.HomeworkConverter;
import com.school.academicservice.dto.HomeworkDTO;
import com.school.academicservice.dto.HomeworkFileDTO;
import com.school.academicservice.entity.Homework;
import com.school.academicservice.entity.HomeworkFile;
import com.school.academicservice.repository.HomeworkFileRepository;
import com.school.academicservice.repository.HomeworkRepository;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class HomeworkService {
    private final HomeworkRepository homeworkRepository;
    private final HomeworkConverter homeworkConverter;
    private final HomeworkFileRepository homeworkFileRepository;

    @Value("${app.homework.storage.path:./deployment/homework}")
    private String storageBasePath;

    @Value("${app.homework.download.base-url:/rest/academic-service}")
    private String downloadBaseUrl;

    public HomeworkDTO createHomework(HomeworkDTO homeworkDTO) {
        log.info("Creating homework: {} for class: {} section: {}", homeworkDTO.getTitle(), homeworkDTO.getClassId(), homeworkDTO.getSectionName());

        Homework homework = homeworkConverter.dtoToEntity(homeworkDTO);
        homework = homeworkRepository.save(homework);
        log.info("Homework created successfully with id: {}", homework.getId());
        return homeworkConverter.entityToDTO(homework);
    }

    public HomeworkDTO createHomework(HomeworkDTO homeworkDTO, List<MultipartFile> files) throws IOException {
        Homework homework = homeworkConverter.dtoToEntity(homeworkDTO);
        addFiles(homework, files);
        Homework savedHomework = homeworkRepository.save(homework);
        refreshDownloadUrls(savedHomework);
        return homeworkConverter.entityToDTO(savedHomework);
    }

    public HomeworkDTO getHomeworkById(Long id) {
        log.info("Fetching homework with id: {}", id);
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homework", "id", id));
        return homeworkConverter.entityToDTO(homework);
    }

    public List<HomeworkDTO> getHomeworkByClassAndSection(Long classId, String sectionName) {
        log.info("Fetching homework for class: {} section: {}", classId, sectionName);
        List<Homework> homeworks = homeworkRepository.findByClassIdAndSectionName(classId, sectionName);
        return homeworks.stream()
                .map(homeworkConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<HomeworkDTO> getHomeworkByTeacher(Long teacherId) {
        log.info("Fetching homework by teacher: {}", teacherId);
        List<Homework> homeworks = homeworkRepository.findByTeacherId(teacherId);
        return homeworks.stream()
                .map(homeworkConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<HomeworkDTO> getUpcomingHomework(LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching upcoming homework between {} and {}", fromDate, toDate);
        List<Homework> homeworks = homeworkRepository.findByDueDateBetween(fromDate, toDate);
        return homeworks.stream()
                .map(homeworkConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public List<HomeworkDTO> byClassSectionAndDueDateBetween(Long classId, String sectionName, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching upcoming homework between {} and {}", fromDate, toDate);
        List<Homework> homeworks = homeworkRepository.findByClassIdAndSectionNameAndDueDateBetween(classId, sectionName, fromDate, toDate);
        return homeworks.stream()
                .map(homeworkConverter::entityToDTO)
                .collect(Collectors.toList());
    }



    public HomeworkDTO updateHomework(Long id, HomeworkDTO homeworkDTO) {
        log.info("Updating homework with id: {}", id);
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homework", "id", id));

        homework.setTitle(homeworkDTO.getTitle());
        homework.setDescription(homeworkDTO.getDescription());
        homework.setFileUrl(homeworkDTO.getFileUrl());
        homework.setDueDate(homeworkDTO.getDueDate());

        homework = homeworkRepository.save(homework);
        log.info("Homework updated successfully with id: {}", homework.getId());
        return homeworkConverter.entityToDTO(homework);
    }

    public HomeworkDTO updateHomework(Long id, HomeworkDTO homeworkDTO, List<MultipartFile> files) throws IOException {
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homework", "id", id));
        homework.setTitle(homeworkDTO.getTitle());
        homework.setDescription(homeworkDTO.getDescription());
        homework.setFileUrl(homeworkDTO.getFileUrl());
        homework.setDueDate(homeworkDTO.getDueDate());
        if (files != null && !files.isEmpty()) {
            homework.getFiles().clear();
            addFiles(homework, files);
        }
        Homework savedHomework = homeworkRepository.save(homework);
        refreshDownloadUrls(savedHomework);
        return homeworkConverter.entityToDTO(savedHomework);
    }

    @Transactional(readOnly = true)
    public HomeworkFileDTO getFile(Long fileId) {
        HomeworkFile file = homeworkFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework file", "id", fileId));
        return HomeworkFileDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .filePath(file.getFilePath())
                .downloadUrl(file.getDownloadUrl())
                .build();
    }

    private void addFiles(Homework homework, List<MultipartFile> files) throws IOException {
        if (files == null) {
            return;
        }

        String schoolCode = Optional.ofNullable(TenantContext.getTenant())
                .filter(code -> !code.isBlank())
                .orElse("default");
        Path schoolStoragePath = Path.of(storageBasePath, sanitizeSchoolCode(schoolCode));
        Files.createDirectories(schoolStoragePath);

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded files must not be empty");
            }

            String originalFileName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String sanitizedFileName = sanitizeFileName(originalFileName);
            String uniqueFileName = UUID.randomUUID() + "_" + sanitizedFileName;
            Path targetPath = schoolStoragePath.resolve(uniqueFileName);
            Files.write(targetPath, file.getBytes());

            homework.getFiles().add(HomeworkFile.builder()
                    .homework(homework)
                    .fileName(originalFileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(targetPath.toString())
                    .downloadUrl(null)
                    .build());
        }
    }

    private void refreshDownloadUrls(Homework homework) {
        if (homework == null || homework.getFiles() == null || homework.getFiles().isEmpty()) {
            return;
        }
        for (HomeworkFile file : homework.getFiles()) {
            if (file.getId() != null) {
                file.setDownloadUrl(buildDownloadUrl(file.getId()));
            }
        }
    }

    private String buildDownloadUrl(Long fileId) {
        String base = Optional.ofNullable(downloadBaseUrl)
                .filter(value -> !value.isBlank())
                .orElse("http://localhost:8000/academic-service");
        String normalizedBase = base.replaceAll("/+$", "");
        return normalizedBase + "/api/v1/homework/files/" + fileId + "/download";
    }

    private String sanitizeSchoolCode(String schoolCode) {
        return schoolCode.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public void deleteHomework(Long id) {
        log.info("Deleting homework with id: {}", id);
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Homework", "id", id));

        for (HomeworkFile file : homework.getFiles()) {
            if (file.getFilePath() != null) {
                try {
                    Files.deleteIfExists(Path.of(file.getFilePath()));
                } catch (IOException e) {
                    log.warn("Failed to delete homework file at path {} for homework id {}", file.getFilePath(), id, e);
                }
            }
        }

        homeworkRepository.deleteById(id);
        log.info("Homework deleted successfully with id: {}", id);
    }
}
