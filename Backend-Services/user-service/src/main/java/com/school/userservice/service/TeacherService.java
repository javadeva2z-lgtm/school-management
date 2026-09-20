package com.school.userservice.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.school.common.enums.UserRole;
import com.school.common.exception.DuplicateResourceException;
import com.school.common.exception.ResourceNotFoundException;
import com.school.userservice.Constants;
import com.school.userservice.Utills;
import com.school.userservice.converter.TeacherConverter;
import com.school.userservice.dto.TeacherDTO;
import com.school.userservice.entity.Teacher;
import com.school.userservice.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final TeacherConverter teacherConverter;
    private final UserService userService;

    public TeacherDTO createTeacher(TeacherDTO teacherDTO) {
        log.info("Creating teacher with employee id: {}", teacherDTO.getEmployeeId());
        
        if (teacherRepository.findByEmployeeIdOrUsername(teacherDTO.getEmployeeId(), teacherDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Teacher", "employeeId/username", teacherDTO.getEmployeeId() + "," + teacherDTO.getUsername());
        }
        
        Teacher teacher = teacherConverter.dtoToEntity(teacherDTO);
        teacher = teacherRepository.save(teacher);
        log.info("Teacher created successfully with id: {}", teacher.getId());
        return teacherConverter.entityToDTO(teacher);
    }

    public TeacherDTO getTeacherById(Long id) {
        log.info("Fetching teacher with id: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        return teacherConverter.entityToDTO(teacher);
    }

    public TeacherDTO getTeacherByUsername(String username) {
        log.info("Fetching teacher with user name: {}", username);
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "username", username));
        return teacherConverter.entityToDTO(teacher);
    }

    public TeacherDTO getTeacherByEmployeeId(String employeeId) {
        log.info("Fetching teacher with employee id: {}", employeeId);
        Teacher teacher = teacherRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "employeeId", employeeId));
        return teacherConverter.entityToDTO(teacher);
    }

    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        log.info("Updating teacher with id: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        teacher.setQualification(teacherDTO.getQualification());
        teacher.setGender(teacherDTO.getGender());
        teacher.setSpecialization(teacherDTO.getSpecialization());
        teacher.setJoiningDate(teacherDTO.getJoiningDate());
        teacher.setExperienceYears(teacherDTO.getExperienceYears());

        teacher = teacherRepository.save(teacher);
        log.info("Teacher updated successfully with id: {}", teacher.getId());
        return teacherConverter.entityToDTO(teacher);
    }

    public void deleteTeacher(Long id) {
        log.info("Deleting teacher with id: {}", id);
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher", "id", id);
        }
        teacherRepository.deleteById(id);
        log.info("Teacher deleted successfully with id: {}", id);
    }

    public List<TeacherDTO> getAllTeachers() {
        log.info("Fetching all teachers");
        List<Teacher> teachers = teacherRepository.findAll();
        return teachers.stream()
                .map(teacherConverter::entityToDTO)
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
            List<Teacher> teachersToSave = new ArrayList<>();

            for (CSVRecord record : csvRecords) {
                String name = record.get(Constants.IMPORT_TEACHER_COLUMN_NAME);

                // Validate that teacher name is mandatory
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException("CSV contains a record with a missing mandatory teacher name.");
                }
                //id,name,admissionNumber,rollNumber,classId,sectionName,fatherName,motherName,dateOfBirth,address,parentPhone,createLogin,isDelete
                long id = record.isMapped(Constants.IMPORT_TEACHER_COLUMN_ID) && !record.get(Constants.IMPORT_TEACHER_COLUMN_ID).isBlank() ? Long.parseLong(record.get(Constants.IMPORT_TEACHER_COLUMN_ID)) : 0;
                String userName = record.get(Constants.IMPORT_TEACHER_COLUMN_USERNAME);
                String employeeId = record.get(Constants.IMPORT_TEACHER_COLUMN_EMPLOYEE_ID);
                String qualification = record.get(Constants.IMPORT_TEACHER_COLUMN_QUALIFICATION);
                String specialization = record.get(Constants.IMPORT_TEACHER_COLUMN_SPECIALIZATION);
                String joiningDate = record.get(Constants.IMPORT_TEACHER_COLUMN_JOINING_DATE);
                String experienceYears = record.get(Constants.IMPORT_TEACHER_COLUMN_EXPERIENCE_YEARS);
                String address = record.get(Constants.IMPORT_TEACHER_COLUMN_ADDRESS);
                String phone = record.get(Constants.IMPORT_TEACHER_COLUMN_PHONE);
                String email = record.isMapped(Constants.IMPORT_TEACHER_COLUMN_EMAIL) ? record.get(Constants.IMPORT_TEACHER_COLUMN_EMAIL) : "";
                String gender = record.isMapped(Constants.IMPORT_TEACHER_COLUMN_GENDER) ? record.get(Constants.IMPORT_TEACHER_COLUMN_GENDER) : "";

                teachersToSave.add(Teacher.builder()
                        .id(id > 0 ? id : null)
                        .name(name)
                        .gender(gender)
                        .email(email)
                        .username(userName)
                        .employeeId(employeeId)
                        .qualification(qualification)
                        .specialization(specialization)
                        .joiningDate(joiningDate != null && !joiningDate.isBlank() ? LocalDate.parse(joiningDate) : null)
                        .experienceYears(experienceYears != null && !experienceYears.isBlank() ? Integer.parseInt(experienceYears) : null)
                        .address(address)
                        .phone(phone)
                        .build());

                        userService.createLoginUser(userName, Utills.generatePasswordFromDateOfBirth(joiningDate != null && !joiningDate.isBlank() ? LocalDate.parse(joiningDate) : LocalDate.of(2000, 01, 01)), UserRole.TEACHER.getValue());
            }

            teacherRepository.saveAll(teachersToSave);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public void exportAllTeachersToCsv(java.io.Writer writer) {
        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(
                Constants.IMPORT_TEACHER_COLUMN_ID,
                Constants.IMPORT_TEACHER_COLUMN_NAME,
                Constants.IMPORT_TEACHER_COLUMN_GENDER,
                Constants.IMPORT_TEACHER_COLUMN_EMAIL,
                Constants.IMPORT_TEACHER_COLUMN_USERNAME,
                Constants.IMPORT_TEACHER_COLUMN_EMPLOYEE_ID,
                Constants.IMPORT_TEACHER_COLUMN_QUALIFICATION,
                Constants.IMPORT_TEACHER_COLUMN_SPECIALIZATION,
                Constants.IMPORT_TEACHER_COLUMN_EXPERIENCE_YEARS,
                Constants.IMPORT_TEACHER_COLUMN_JOINING_DATE,
                Constants.IMPORT_TEACHER_COLUMN_PHONE,
                Constants.IMPORT_TEACHER_COLUMN_ADDRESS
            ).build();

            List<Teacher> teachers = teacherRepository.findAll();
            csvFormat.print(writer).printRecords(teachers.stream().map(t -> new Object[]{
                    t.getId(),
                    t.getName(),
                    t.getGender(),
                    t.getEmail(),
                    t.getUsername(),
                    t.getEmployeeId(),
                    t.getQualification(),
                    t.getSpecialization(),
                    t.getExperienceYears(),
                    t.getJoiningDate(),
                    t.getPhone(),
                    t.getAddress()
            }).toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export teachers to CSV: " + e.getMessage());
        }
    }
}
