package com.school.academicservice.repository;

import com.school.academicservice.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findByClassIdAndSectionName(Long classId, String sectionName);
    List<Homework> findByTeacherId(Long teacherId);
    List<Homework> findByDueDateBetween(LocalDate fromDate, LocalDate toDate);
    List<Homework> findByClassIdAndSectionNameAndSubjectId(Long classId, String sectionName, Long subjectId);
    
    List<Homework> findByClassIdAndSectionNameAndDueDateBetween(Long classId, String sectionName, LocalDate fromDate, LocalDate toDate);
}
