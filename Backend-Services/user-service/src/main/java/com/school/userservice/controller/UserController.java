package com.school.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.common.response.ApiResponse;
import com.school.userservice.dto.LoginResponseDTO;
import com.school.userservice.dto.ProfileDTO;
import com.school.userservice.dto.UserRegistrationDTO;
import com.school.userservice.service.ProfileService;
import com.school.userservice.service.SchoolService;
import com.school.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final SchoolService schoolService;
    private final ProfileService profileService;

    @PostMapping("/register/admin/{token}")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> registerAdminForSchool(@Valid @RequestBody UserRegistrationDTO registrationDTO, @PathVariable String token) {
        log.info("Register request received for username: {}", registrationDTO.getUsername());
        
        LoginResponseDTO response = userService.registerAdmin(registrationDTO, token);
        // Clear the token after successful registration to prevent reuse
        schoolService.clearToken();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Admin registered successfully"));
    }

    @GetMapping("/register/admin/token")
    @Operation(summary = "Get registration token for admin")
    public ResponseEntity<ApiResponse<String>> getToken() {
        String token = schoolService.getToken();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(token, "Token retrieved successfully"));
    }

    @GetMapping("/register/admin/isEnable")
    @Operation(summary = "check if a new admin creation is enabled")
    public ResponseEntity<ApiResponse<Boolean>> checkAdminCreationEnabled() {
        log.info("Checking admin creation enabled status");
        schoolService.getToken();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(true, "Admin creation status retrieved successfully"));
    }

    @PostMapping("/register")
    @PreAuthorize ("hasRole('ADMIN')")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        log.info("Register request received for username: {}", registrationDTO.getUsername());
        LoginResponseDTO response = userService.register(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }
    

    @GetMapping("/disable/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "disable user login")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> disableLogin(@PathVariable String username) {
        userService.updateUserStatus(username, false);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Login disabled successfully"));
    }
    
    @GetMapping("/enable/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "activate user for login")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> enableLogin(@PathVariable String username) {
        userService.updateUserStatus(username, true);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Login enabled successfully"));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ProfileDTO>> getProfile() {
        ProfileDTO profile = profileService.getProfile();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(profile, "User profile retrieved successfully"));
    }
}
