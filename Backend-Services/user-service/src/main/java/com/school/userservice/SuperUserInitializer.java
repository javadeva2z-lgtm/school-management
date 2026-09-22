package com.school.userservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.school.common.enums.UserRole;
import com.school.common.enums.UserRoleWithRolePrefix;
import com.school.userservice.entity.User;
import com.school.userservice.repository.UserRepository;
import com.school.userservice.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository roleRepository;

    @Value("${app.superuser.username}")
    private String adminUsername;

    @Value("${app.superuser.email}")
    private String adminEmail;

    @Value("${app.superuser.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        // Check if a SUPER_ADMIN already exists (or use userRepository.count() == 0 if
        // you want to check for any user)
        if (!roleRepository.existsByRole(UserRoleWithRolePrefix.SUPER_ADMIN.getValue())) {
            log.info("No SUPER_ADMIN found in system. Seeding default super user...");

            User superAdmin = new User();
            superAdmin.setUsername(adminUsername);
            superAdmin.setPhoneNumber(adminEmail);
            superAdmin.setPassword(passwordEncoder.encode(adminPassword));
            superAdmin.setIsActive(true);

            userRepository.save(superAdmin);

            com.school.userservice.entity.UserRole role = com.school.userservice.entity.UserRole.builder()
                    .role("ROLE_"+UserRole.SUPER_ADMIN.getValue()).username(adminUsername).build();
            roleRepository.save(role);

            log.info("Default SUPER_ADMIN created successfully with username: {}", adminUsername);
        } else {
            log.info("SUPER_ADMIN already exists. Skipping initialization.");
        }
    }
}