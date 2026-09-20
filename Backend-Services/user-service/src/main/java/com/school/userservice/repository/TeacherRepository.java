package com.school.userservice.repository;

import com.school.userservice.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUsername(String username);
    Optional<Teacher> findByEmployeeId(String employeeId);
    Optional<Teacher> findByEmployeeIdOrUsername(String employeeId, String username);
}
