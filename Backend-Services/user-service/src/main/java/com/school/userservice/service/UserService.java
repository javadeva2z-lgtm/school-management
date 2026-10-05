package com.school.userservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pawan.share.jwt.JwtUtil;
import com.school.common.exception.DuplicateResourceException;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.service.BaseService;
import com.school.userservice.dto.LoginRequestDTO;
import com.school.userservice.dto.LoginResponseDTO;
import com.school.userservice.dto.ManagedUserDTO;
import com.school.userservice.dto.PasswordManagedUserDTO;
import com.school.userservice.dto.UserRegistrationDTO;
import com.school.userservice.entity.User;
import com.school.userservice.entity.UserRole;
import com.school.userservice.repository.UserRepository;
import com.school.userservice.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService extends BaseService {
    private static final Set<String> PASSWORD_MANAGED_ROLES = Set.of(
            "ROLE_STUDENT", "ROLE_TEACHER", "ROLE_MANAGER");
    private static final Set<String> ADMIN_ROLES = Set.of("ROLE_ADMIN", "ROLE_SUPER_ADMIN");

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

    public LoginResponseDTO registerAdminOrManager(UserRegistrationDTO registrationDTO) {
        String requestedRole = registrationDTO.getRole() == null
                ? ""
                : registrationDTO.getRole().trim().toUpperCase(Locale.ROOT);
        if (!Set.of(com.school.common.enums.UserRole.ADMIN.getValue(),
                com.school.common.enums.UserRole.MANAGER.getValue()).contains(requestedRole)) {
            throw new IllegalArgumentException("Only ADMIN and MANAGER accounts can be created here");
        }
        registrationDTO.setRole(requestedRole);
        return register(registrationDTO);
    }

    @Transactional(readOnly = true)
    public List<ManagedUserDTO> getManagers() {
        Set<String> usernames = userRoleRepository.findByRole("ROLE_MANAGER").stream()
                .map(UserRole::getUsername)
                .collect(Collectors.toSet());
        if (usernames.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllByUsernameIn(usernames).stream()
                .map(user -> ManagedUserDTO.builder()
                        .username(user.getUsername())
                        .phoneNumber(user.getPhoneNumber())
                        .build())
                .sorted((left, right) -> left.getUsername().compareToIgnoreCase(right.getUsername()))
                .toList();
    }

    public void deleteManager(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "username", username));
        if (!userRoleRepository.existsByUsernameAndRole(username, "ROLE_MANAGER")) {
            throw new ResourceNotFoundException("Manager", "username", username);
        }

        userRoleRepository.deleteByUsernameAndRole(username, "ROLE_MANAGER");
        if (!userRoleRepository.existsByUsername(username)) {
            userRepository.delete(user);
        }
        log.info("Manager account deleted: {}", username);
    }

    public void resetPassword(UserRegistrationDTO registrationDTO) {
        User user = userRepository.findByToken(registrationDTO.getToken())
                .orElseThrow(() -> new UsernameNotFoundException(""));
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));

        userRepository.save(user);
    }

    public void changeOwnPassword(String currentPassword, String newPassword) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("An authenticated user is required to change the password");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed by user: {}", username);
    }

    @Transactional(readOnly = true)
    public List<PasswordManagedUserDTO> getPasswordManagedUsers() {
        requireAdministrator();
        Map<String, Set<String>> rolesByUsername = userRoleRepository.findAll().stream()
                .filter(userRole -> PASSWORD_MANAGED_ROLES.contains(userRole.getRole()))
                .collect(Collectors.groupingBy(
                        UserRole::getUsername,
                        Collectors.mapping(UserRole::getRole, Collectors.toSet())));
        Set<String> adminUsernames = userRoleRepository.findAll().stream()
                .filter(userRole -> ADMIN_ROLES.contains(userRole.getRole()))
                .map(UserRole::getUsername)
                .collect(Collectors.toSet());

        return userRepository.findAllByUsernameIn(rolesByUsername.keySet()).stream()
                .filter(user -> !adminUsernames.contains(user.getUsername()))
                .map(user -> PasswordManagedUserDTO.builder()
                        .username(user.getUsername())
                        .role(rolesByUsername.get(user.getUsername()).stream()
                                .map(role -> role.substring("ROLE_".length()))
                                .sorted()
                                .collect(Collectors.joining(", ")))
                        .build())
                .sorted((left, right) -> left.getUsername().compareToIgnoreCase(right.getUsername()))
                .toList();
    }

    public void adminResetPassword(String username, String newPassword) {
        requireAdministrator();
        Set<String> userRoles = userRoleRepository.findByUsername(username).stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
        if (userRoles.stream().noneMatch(PASSWORD_MANAGED_ROLES::contains)
                || userRoles.stream().anyMatch(ADMIN_ROLES::contains)) {
            throw new ResourceNotFoundException("Password-manageable user", "username", username);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset by administrator for user: {}", username);
    }

    private void requireAdministrator() {
        if (!hasRole(com.school.common.enums.UserRole.ADMIN)) {
            throw new AccessDeniedException("Only administrators can reset other users' passwords");
        }
    }

    public void setResetPasswordToken(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(""));
        user.setToken(UUID.randomUUID().toString());
        // notify to user using phone/email
        userRepository.save(user);
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
                || Objects.equals(com.school.common.enums.UserRole.ADMIN.getValue(), role)
                || Objects.equals(com.school.common.enums.UserRole.MANAGER.getValue(), role);
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
