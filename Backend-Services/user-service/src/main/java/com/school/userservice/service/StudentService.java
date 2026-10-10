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
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.school.common.enums.UserRole;
import com.school.common.exception.DuplicateResourceException;
import com.school.common.exception.ResourceNotFoundException;
import com.school.userservice.Constants;
import com.school.userservice.Utills;
import com.school.userservice.converter.StudentConverter;
import com.school.userservice.dto.StudentDTO;
import com.school.userservice.entity.Student;
import com.school.userservice.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;
    private final StudentConverter studentConverter;
    private final UserService userService;

    public StudentDTO createStudent(StudentDTO studentDTO) {
        log.info("Creating student with roll number: {}", studentDTO.getRollNumber());

        if (studentRepository.findByRollNumber(studentDTO.getRollNumber()).isPresent()) {
            throw new DuplicateResourceException("Student", "rollNumber", studentDTO.getRollNumber());
        }

        Student student = studentConverter.dtoToEntity(studentDTO);
        student = studentRepository.save(student);
        log.info("Student created successfully with admission number: {}", student.getAdmissionNumber());
        return studentConverter.entityToDTO(student);
    }

    public StudentDTO getStudentByAdmissionNumber(Long admissionNumber) {
        log.info("Fetching student with admission number: {}", admissionNumber);
        Student student = studentRepository.findById(admissionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "admissionNumber", admissionNumber));
        return studentConverter.entityToDTO(student);
    }

    public StudentDTO getStudentByUsernameOrAdmNumber(Long admissionNumber) {
        log.info("Fetching student with admissionNumber/username: {}", admissionNumber);
        Student student = studentRepository.findByAdmissionNumber(admissionNumber)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Student", "admissionNumber/username", admissionNumber));
        return studentConverter.entityToDTO(student);
    }

    public StudentDTO getStudentByRollNumber(Long rollNumber) {
        log.info("Fetching student with roll number: {}", rollNumber);
        Student student = studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "rollNumber", rollNumber));
        return studentConverter.entityToDTO(student);
    }

    public List<StudentDTO> getStudentsByClassAndSection(Long classId, String secName) {
        log.info("Fetching students for class: {} and section: {}", classId, secName);
        List<Student> students = studentRepository.findByClassIdAndSectionName(classId, secName);
        return students.stream()
                .map(studentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public Page<StudentDTO> getStudentsByClassAndSection(Long classId, String secName, Pageable pageable) {
        log.info("Fetching paginated students for class: {} and section: {}", classId, secName);
        Page<Student> students = studentRepository.findByClassIdAndSectionName(classId, secName, pageable);
        return students.map(studentConverter::entityToDTO);
    }

    public StudentDTO updateStudent(Long admissionNumber, StudentDTO studentDTO) {
        log.info("Updating student with admission number: {}", admissionNumber);
        Student student = studentRepository.findById(admissionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "admissionNumber", admissionNumber));
        student.setFatherName(studentDTO.getFatherName());
        student.setMotherName(studentDTO.getMotherName());
        student.setGender(studentDTO.getGender());
        student.setDateOfBirth(studentDTO.getDateOfBirth());
        student.setAdmissionDate(studentDTO.getAdmissionDate());
        student.setAddress(studentDTO.getAddress());
        student.setParentPhone(studentDTO.getParentPhone());
        student.setEmail(studentDTO.getEmail());
        student.setRollNumber(studentDTO.getRollNumber());
        student.setClassId(studentDTO.getClassId());
        student.setSectionName(studentDTO.getSectionName());
        student.setIsEWS(studentDTO.getIsEws());

        student = studentRepository.save(student);
        log.info("Student updated successfully with admission number: {}", student.getAdmissionNumber());
        return studentConverter.entityToDTO(student);
    }

    public void deleteStudent(Long admissionNumber) {
        log.info("Deleting student with admission number: {}", admissionNumber);
        if (!studentRepository.existsById(admissionNumber)) {
            throw new ResourceNotFoundException("Student", "admissionNumber", admissionNumber);
        }
        studentRepository.deleteById(admissionNumber);
        log.info("Student deleted successfully with admission number: {}", admissionNumber);
    }

    public void deleteStudent(Student student) {
        studentRepository.delete(student);
        log.info("Student deleted successfully with admission number: {}", student.getAdmissionNumber());
    }

    public List<StudentDTO> getAllStudents() {
        log.info("Fetching all students");
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(studentConverter::entityToDTO)
                .collect(Collectors.toList());
    }

    public void importCsv(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .build();

            Iterable<CSVRecord> csvRecords = csvFormat.parse(fileReader);
            for (CSVRecord record : csvRecords) {
                String name = record.get(Constants.IMPORT_STUDENT_COLUMN_NAME);

                // Validate that student name is mandatory
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException("CSV contains a record with a missing mandatory student name.");
                }
                // admissionNumber,name,rollNumber,classId,sectionName,fatherName,motherName,dateOfBirth,admissionDate,address,parentPhone,createLogin,isDelete
                boolean createLogin = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_CREATE_LOGIN)
                        ? BooleanUtils.toBoolean(record.get(Constants.IMPORT_STUDENT_COLUMN_CREATE_LOGIN))
                        : false;
                boolean isDelete = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_IS_DELETE)
                        ? BooleanUtils.toBoolean(record.get(Constants.IMPORT_STUDENT_COLUMN_IS_DELETE))
                        : false;
                String admissionNumberText = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ADMISSION_NUMBER)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_ADMISSION_NUMBER).trim()
                        : "";
                Long admissionNumber = admissionNumberText.isEmpty() ? null : Long.valueOf(admissionNumberText);
                Long rollNumber = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ROLL_NUMBER)
                        ? Long.valueOf(record.get(Constants.IMPORT_STUDENT_COLUMN_ROLL_NUMBER))
                        : null;
                Long classId = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_CLASS_ID)
                        ? Long.valueOf(record.get(Constants.IMPORT_STUDENT_COLUMN_CLASS_ID))
                        : null;
                String sectionName = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_SECTION_NAME)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_SECTION_NAME)
                        : "";
                String fatherName = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_FATHER_NAME)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_FATHER_NAME)
                        : "";
                String motherName = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_MOTHER_NAME)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_MOTHER_NAME)
                        : "";
                String dateOfBirth = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_DOB)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_DOB)
                        : "";
                String admissionDate = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ADMISSION_DATE)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_ADMISSION_DATE)
                        : "";
                String address = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ADDRESS)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_ADDRESS)
                        : "";
                String parentPhone = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_PARENT_PHONE)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_PARENT_PHONE)
                        : "";
                String email = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_EMAIL)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_EMAIL)
                        : "";
                String gender = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_GENDER)
                        ? record.get(Constants.IMPORT_STUDENT_COLUMN_GENDER)
                        : "";
                LocalDate dateOfBirthValue = parseCsvDate(dateOfBirth);
                LocalDate admissionDateValue = parseCsvDate(admissionDate);

                Student existingStudent = admissionNumber == null
                        ? null
                        : studentRepository.findByAdmissionNumber(admissionNumber).orElse(null);
                if (existingStudent != null) {
                    if (isDelete) {
                        deleteStudent(existingStudent);
                        continue;
                    } else {
                        existingStudent.setName(name);
                        existingStudent.setGender(gender);
                        existingStudent.setRollNumber(rollNumber);
                        existingStudent.setClassId(classId);
                        existingStudent.setSectionName(sectionName);
                        existingStudent.setFatherName(fatherName);
                        existingStudent.setMotherName(motherName);
                        existingStudent.setDateOfBirth(dateOfBirthValue);
                        if (record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ADMISSION_DATE)
                                && admissionDateValue != null) {
                            existingStudent.setAdmissionDate(admissionDateValue);
                        }
                        existingStudent.setAddress(address);
                        existingStudent.setParentPhone(parentPhone);
                        existingStudent.setEmail(email);
                        Student savedStudent = studentRepository.save(existingStudent);
                        if (createLogin) {
                            userService.createLoginUser(String.valueOf(savedStudent.getAdmissionNumber()),
                                    Utills.generatePasswordFromDateOfBirth(dateOfBirthValue),
                                    UserRole.STUDENT.getValue());
                        }
                        continue;
                    }
                } else if (isDelete) {
                    continue;
                }
                Student student = Student.builder()
                        .name(name)
                        .gender(gender)
                        .rollNumber(rollNumber)
                        .classId(classId)
                        .sectionName(sectionName)
                        .fatherName(fatherName)
                        .motherName(motherName)
                        // Date must be in YYYY-MM-DD format, if not then it will throw an exception
                        .dateOfBirth(dateOfBirthValue)
                        .admissionDate(admissionDateValue)
                        .address(address)
                        .parentPhone(parentPhone)
                        .email(email)
                        .build();
                Student savedStudent = studentRepository.save(student);
                if (createLogin) {
                    userService.createLoginUser(String.valueOf(savedStudent.getAdmissionNumber()),
                            Utills.generatePasswordFromDateOfBirth(dateOfBirthValue), UserRole.STUDENT.getValue());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public void exportAllStudentsToCsv(java.io.Writer writer, Long classId, String sectionName) {
        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(
                    Constants.IMPORT_STUDENT_COLUMN_ADMISSION_NUMBER,
                    Constants.IMPORT_STUDENT_COLUMN_NAME,
                    Constants.IMPORT_STUDENT_COLUMN_GENDER,
                    Constants.IMPORT_STUDENT_COLUMN_EMAIL,
                    Constants.IMPORT_STUDENT_COLUMN_ROLL_NUMBER,
                    Constants.IMPORT_STUDENT_COLUMN_CLASS_ID,
                    Constants.IMPORT_STUDENT_COLUMN_SECTION_NAME,
                    Constants.IMPORT_STUDENT_COLUMN_FATHER_NAME,
                    Constants.IMPORT_STUDENT_COLUMN_MOTHER_NAME,
                    Constants.IMPORT_STUDENT_COLUMN_DOB,
                    Constants.IMPORT_STUDENT_COLUMN_ADMISSION_DATE,
                    Constants.IMPORT_STUDENT_COLUMN_ADDRESS,
                    Constants.IMPORT_STUDENT_COLUMN_PARENT_PHONE,
                    Constants.IMPORT_STUDENT_COLUMN_CREATE_LOGIN,
                    Constants.IMPORT_STUDENT_COLUMN_IS_DELETE).build();

            List<Student> students = new ArrayList<>();
            if (classId == null || classId == 0) {
                students = studentRepository.findAll();
            } else if (sectionName == null || sectionName.isBlank() || "0".equals(sectionName)) {
                students = studentRepository.findByClassId(classId);
            } else {
                students = studentRepository.findByClassIdAndSectionName(classId, sectionName);
            }
            csvFormat.print(writer).printRecords(students.stream().map(s -> new Object[] {
                    s.getAdmissionNumber(),
                    s.getName(),
                    nullToEmpty(s.getGender()),
                    s.getEmail(),
                    s.getRollNumber(),
                    s.getClassId(),
                    s.getSectionName(),
                    nullToEmpty(s.getFatherName()),
                    nullToEmpty(s.getMotherName()),
                    s.getDateOfBirth(),
                    s.getAdmissionDate(),
                    nullToEmpty(s.getAddress()),
                    nullToEmpty(s.getParentPhone()),
                    0,
                    0
            }).toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export students to CSV: " + e.getMessage());
        }
    }

    private String nullToEmpty(String s) {
        s = (s == null) ? "" : s;
        return s;
    }

    private LocalDate parseCsvDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException exception) {
            return Utills.getDateFromString(value);
        }
    }
}
