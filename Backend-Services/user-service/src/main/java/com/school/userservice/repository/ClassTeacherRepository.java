package com.school.userservice.repository;

import com.school.userservice.entity.ClassTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClassTeacherRepository extends JpaRepository<ClassTeacher, Long> {
    Optional<ClassTeacher> findByClassIdAndSectionName(Long classId, String sectionName);
    Optional<ClassTeacher> findByTeacherId(Long teacherId);
}
