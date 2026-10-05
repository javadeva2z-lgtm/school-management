package com.school.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.userservice.entity.ClassSubject;

@Repository
public interface ClassSubjectRepository extends JpaRepository<ClassSubject, Long> {
    List<ClassSubject> findByClassId(Long classId);

    Optional<ClassSubject> findByClassIdAndSubjectCode(Long classId, String subjectCode);

    boolean existsByClassIdAndSubjectCodeAndIdNot(Long classId, String subjectCode, Long id);
}
