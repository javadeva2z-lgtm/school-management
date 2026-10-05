package com.school.userservice.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.userservice.entity.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Set<UserRole> findByUsername(String usename);

    Set<UserRole> findByRole(String role);

    Boolean existsByUsernameAndRole(String userId, String role);

    boolean existsByUsername(String username);

    void deleteByUsernameAndRole(String username, String role);

    boolean existsByRole(String role);

}
