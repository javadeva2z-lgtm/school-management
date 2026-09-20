package com.school.userservice.repository;

import com.school.userservice.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    Optional<Class> findByClassNameAndAcademicYear(String className, String academicYear);
    List<Class> findByIsActiveTrue();
    Optional<Class> findByClassId(Long classId);
    void deleteByClassId(Long classId);
    boolean existsByClassId(Long classId);
}
