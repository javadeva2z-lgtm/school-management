package com.school.userservice.service;

import com.school.userservice.dto.LoginRequestDTO;
import com.school.userservice.dto.LoginResponseDTO;
import com.school.userservice.dto.ProfileDTO;
import com.school.userservice.dto.ProfileDTO.ProfileDTOBuilder;
import com.school.userservice.dto.StudentDTO;
import com.school.userservice.dto.TeacherDTO;
import com.school.userservice.dto.UserRegistrationDTO;
import com.school.userservice.entity.User;
import com.school.userservice.entity.UserRole;
import com.school.userservice.repository.UserRepository;
import com.school.userservice.repository.UserRoleRepository;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.service.BaseService;
import com.pawan.share.jwt.JwtUtil;
import com.school.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService extends BaseService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SchoolService schoolService;
    private final JwtUtil jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO registerAdmin(UserRegistrationDTO registrationDTO, String token) {
        if (!schoolService.validateToken(token)) {
            throw new RuntimeException("Invalid token for admin registration");
        }
        registrationDTO.setRole("ADMIN");
        return register(registrationDTO);
    }

    public LoginResponseDTO register(UserRegistrationDTO registrationDTO) {

        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new DuplicateResourceException("User", "username", registrationDTO.getUsername());
        }

        User user = User.builder()
                .username(registrationDTO.getUsername())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .phoneNumber(registrationDTO.getPhoneNumber())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());

        // Assign role
        UserRole userRole = UserRole.builder()
                .username(user.getUsername())
                .role("ROLE_" + registrationDTO.getRole())
                .build();
        userRoleRepository.save(userRole);
        log.info("Role {} assigned to user id: {}", registrationDTO.getRole(), user.getId());

        return LoginResponseDTO.builder()
                .username(user.getUsername())
                .isActive(true)
                .build();
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Attempting login for username: {}", loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", loginRequest.getUsername()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", loginRequest.getUsername());
            throw new RuntimeException("Invalid username or password");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        List<String> roles = userRoleRepository.findByUsername(user.getUsername())
                .stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        Map<String, String> claimMap = new HashMap<>();

        String schoolCode = getSchoolCodeFromRequestHeader();

        log.info("Generating JWT token for user: {} with roles: {} and schoolCode: {}", user.getUsername(), roles,
                schoolCode);
        claimMap.put("uid", String.valueOf(user.getId()));
        claimMap.put("schoolCode", schoolCode);

        String token = jwtTokenProvider.generateToken(user.getUsername(), roles, claimMap);
        log.info("User logged in successfully: {}", user.getId());

        return LoginResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .token(token)
                .roles(roles)
                .isActive(user.getIsActive())
                .build();
    }

    public void updateUserStatus(String userName, boolean isActive) {
        log.info("Attempting update user status for username: {}", userName);

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userName", userName));
        if (!Objects.equals(isActive, user.getIsActive())) {
            user.setIsActive(isActive);
            userRepository.save(user);
        }
    }

    public void createLoginUser(String userName, String password, String role) {
        boolean isValidRole = Objects.equals(com.school.common.enums.UserRole.STUDENT.getValue(), role)
                || Objects.equals(com.school.common.enums.UserRole.TEACHER.getValue(), role)
                || Objects.equals(com.school.common.enums.UserRole.ADMIN.getValue(), role);
        try {
            UserRegistrationDTO userRegistrationDTO = UserRegistrationDTO.builder()
                    .username(userName)
                    .password(password)
                    .phoneNumber("0000000000")
                    .role(isValidRole ? role : com.school.common.enums.UserRole.STUDENT.getValue())
                    .build();
            register(userRegistrationDTO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create login user: " + e.getMessage());
        }
    }
}
