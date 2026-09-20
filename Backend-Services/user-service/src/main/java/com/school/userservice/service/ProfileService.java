package com.school.userservice.service;

import com.school.userservice.dto.ProfileDTO;
import com.school.userservice.dto.ProfileDTO.ProfileDTOBuilder;
import com.school.userservice.dto.StudentDTO;
import com.school.userservice.dto.TeacherDTO;
import com.school.common.enums.UserRole;
import com.school.common.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProfileService extends BaseService {
    private final TeacherService teacherService;
    private final StudentService studentService;

    public ProfileDTO getProfile() {
        String username = getCurrentUsername();
        ProfileDTOBuilder profileBuilder = ProfileDTO.builder();
        if (hasRole(UserRole.TEACHER)) {
            TeacherDTO teacherDTO = teacherService.getTeacherByUsername(username);
            profileBuilder.teacher(teacherDTO);

        } else if (hasRole(UserRole.STUDENT)) {
            if (username.trim().matches("-?\\d+")) {
                StudentDTO studentDTO = studentService.getStudentByUsernameOrAdmNumber(Long.parseLong(username.trim()));
                profileBuilder.student(studentDTO);
            }
        }else if (hasRole(UserRole.ADMIN)) {
            profileBuilder.admin(true);
        }

        return profileBuilder.build();
    }
}
