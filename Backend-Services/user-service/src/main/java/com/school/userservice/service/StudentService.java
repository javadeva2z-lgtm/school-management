package com.school.userservice.service;

import com.school.userservice.dto.StudentDTO;
import com.school.userservice.dto.UserRegistrationDTO;
import com.school.userservice.entity.Student;
import com.school.userservice.entity.User;
import com.school.userservice.repository.StudentRepository;
import com.school.userservice.Constants;
import com.school.userservice.Utills;
import com.school.userservice.converter.StudentConverter;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.enums.UserRole;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        log.info("Student created successfully with id: {}", student.getId());
        return studentConverter.entityToDTO(student);
    }

    public StudentDTO getStudentById(Long id) {
        log.info("Fetching student with id: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return studentConverter.entityToDTO(student);
    }

    public StudentDTO getStudentByUsernameOrAdmNumber(Long admissionNumber) {
        log.info("Fetching student with admissionNumber/username: {}", admissionNumber);
        Student student = studentRepository.findByAdmissionNumber(admissionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "admissionNumber/username", admissionNumber));
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

    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        log.info("Updating student with id: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        student.setFatherName(studentDTO.getFatherName());
        student.setMotherName(studentDTO.getMotherName());
        student.setGender(studentDTO.getGender());
        student.setDateOfBirth(studentDTO.getDateOfBirth());
        student.setAddress(studentDTO.getAddress());
        student.setParentPhone(studentDTO.getParentPhone());

        student = studentRepository.save(student);
        log.info("Student updated successfully with id: {}", student.getId());
        return studentConverter.entityToDTO(student);
    }

    public void deleteStudent(Long id) {
        log.info("Deleting student with id: {}", id);
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", "id", id);
        }
        studentRepository.deleteById(id);
        log.info("Student deleted successfully with id: {}", id);
    }
    public void deleteStudent(Student student) {
        studentRepository.delete(student);
        log.info("Student deleted successfully with id: {}", student.getId());
    }


    public List<StudentDTO> getAllStudents() {
        log.info("Fetching all students");
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(studentConverter::entityToDTO)
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
            List<Student> studentsToSave = new ArrayList<>();

            for (CSVRecord record : csvRecords) {
                String name = record.get(Constants.IMPORT_STUDENT_COLUMN_NAME);
                
                // Validate that student name is mandatory
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException("CSV contains a record with a missing mandatory student name.");
                }
                //id,name,admissionNumber,rollNumber,classId,sectionName,fatherName,motherName,dateOfBirth,address,parentPhone,createLogin,isDelete
                long id = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ID) && !record.get(Constants.IMPORT_STUDENT_COLUMN_ID).isBlank() ? Long.parseLong(record.get(Constants.IMPORT_STUDENT_COLUMN_ID)) : 0;
                boolean createLogin = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_CREATE_LOGIN) ? BooleanUtils.toBoolean(record.get(Constants.IMPORT_STUDENT_COLUMN_CREATE_LOGIN)) : false;
                boolean isDelete = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_IS_DELETE) ? BooleanUtils.toBoolean(record.get(Constants.IMPORT_STUDENT_COLUMN_IS_DELETE)) : false;
                Long admissionNumber = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ADMISSION_NUMBER) ? Long.valueOf(record.get(Constants.IMPORT_STUDENT_COLUMN_ADMISSION_NUMBER)) : null;
                Long rollNumber = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ROLL_NUMBER) ? Long.valueOf(record.get(Constants.IMPORT_STUDENT_COLUMN_ROLL_NUMBER)) : null;
                Long classId = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_CLASS_ID) ? Long.valueOf(record.get(Constants.IMPORT_STUDENT_COLUMN_CLASS_ID)) : null;
                String sectionName = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_SECTION_NAME) ? record.get(Constants.IMPORT_STUDENT_COLUMN_SECTION_NAME) : "";
                String fatherName = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_FATHER_NAME) ? record.get(Constants.IMPORT_STUDENT_COLUMN_FATHER_NAME) : "";
                String motherName = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_MOTHER_NAME) ? record.get(Constants.IMPORT_STUDENT_COLUMN_MOTHER_NAME) : "";
                String dateOfBirth = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_DOB) ? record.get(Constants.IMPORT_STUDENT_COLUMN_DOB) : "";
                String address = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_ADDRESS) ? record.get(Constants.IMPORT_STUDENT_COLUMN_ADDRESS) : "";
                String parentPhone = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_PARENT_PHONE) ? record.get(Constants.IMPORT_STUDENT_COLUMN_PARENT_PHONE) : "";
                String email = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_EMAIL) ? record.get(Constants.IMPORT_STUDENT_COLUMN_EMAIL) : "";
                String gender = record.isMapped(Constants.IMPORT_STUDENT_COLUMN_GENDER) ? record.get(Constants.IMPORT_STUDENT_COLUMN_GENDER) : "";
                LocalDate dateOfBirthValue = Utills.getDateFromString(dateOfBirth);

                if(createLogin) {
                    userService.createLoginUser(String.valueOf(admissionNumber), Utills.generatePasswordFromDateOfBirth(dateOfBirthValue), UserRole.STUDENT.getValue());
                }

                if(id > 0) {
                    Student existingStudent = studentRepository.findById(id).orElse(null);
                    if (existingStudent != null) {
                        if (isDelete) {
                            deleteStudent(existingStudent);
                            continue;
                        } else {
                            existingStudent.setName(name);
                            existingStudent.setGender(gender);
                            existingStudent.setAdmissionNumber(admissionNumber);
                            existingStudent.setRollNumber(rollNumber);
                            existingStudent.setClassId(classId);
                            existingStudent.setSectionName(sectionName);
                            existingStudent.setFatherName(fatherName);
                            existingStudent.setMotherName(motherName);
                            //Date must be in DD-MM-YYYY format, if not then it will throw an exception
                            existingStudent.setDateOfBirth(dateOfBirthValue);
                            existingStudent.setAddress(address);
                            existingStudent.setParentPhone(parentPhone);
                            existingStudent.setEmail(email);
                            studentsToSave.add(existingStudent);
                            continue;
                        }
                    }else if (isDelete) {
                        continue;
                    }
                }
                Student student = Student.builder()
                        .name(name)
                        .gender(gender)
                        .admissionNumber(admissionNumber)
                        .rollNumber(rollNumber)
                        .classId(classId)
                        .sectionName(sectionName)
                        .fatherName(fatherName)
                        .motherName(motherName)
                        //Date must be in YYYY-MM-DD format, if not then it will throw an exception
                        .dateOfBirth(dateOfBirthValue)
                        .address(address)
                        .parentPhone(parentPhone)
                        .email(email)
                        .build();
                studentsToSave.add(student);
            }
            studentRepository.saveAll(studentsToSave);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public void exportAllStudentsToCsv(java.io.Writer writer, Long classId, String sectionName) {
        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(
                Constants.IMPORT_STUDENT_COLUMN_ID,
                Constants.IMPORT_STUDENT_COLUMN_NAME,
                Constants.IMPORT_STUDENT_COLUMN_GENDER,
                Constants.IMPORT_STUDENT_COLUMN_EMAIL,
                Constants.IMPORT_STUDENT_COLUMN_ROLL_NUMBER,
                Constants.IMPORT_STUDENT_COLUMN_ADMISSION_NUMBER,
                Constants.IMPORT_STUDENT_COLUMN_CLASS_ID,
                Constants.IMPORT_STUDENT_COLUMN_SECTION_NAME,
                Constants.IMPORT_STUDENT_COLUMN_FATHER_NAME,
                Constants.IMPORT_STUDENT_COLUMN_MOTHER_NAME,
                Constants.IMPORT_STUDENT_COLUMN_DOB,
                Constants.IMPORT_STUDENT_COLUMN_ADDRESS,
                Constants.IMPORT_STUDENT_COLUMN_PARENT_PHONE,
                Constants.IMPORT_STUDENT_COLUMN_CREATE_LOGIN,
                Constants.IMPORT_STUDENT_COLUMN_IS_DELETE
            ).build();

            List<Student> students = new ArrayList<>();
            if(classId == null || classId == 0) {
            	students = studentRepository.findAll();
            }
            else if (classId != null && sectionName != null) {
                students = studentRepository.findByClassIdAndSectionName(classId, sectionName);
            } else if (classId != null) {
                students = studentRepository.findByClassId(classId);
            }
            csvFormat.print(writer).printRecords(students.stream().map(s -> new Object[]{
                    s.getId(),
                    s.getName(),
                    nullToEmpty(s.getGender()),
                    s.getEmail(),
                    s.getRollNumber(),
                    s.getAdmissionNumber(),
                    s.getClassId(),
                    s.getSectionName(),
                    nullToEmpty(s.getFatherName()),
                    nullToEmpty(s.getMotherName()),
                    s.getDateOfBirth(),
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
}
