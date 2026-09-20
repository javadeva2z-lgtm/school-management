package com.school.academicservice.repository;

import com.school.academicservice.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    List<ExamSchedule> findByClassIdAndSectionName(Long classId, String sectionName);
    List<ExamSchedule> findByExamDateBetween(LocalDate fromDate, LocalDate toDate);
    List<ExamSchedule> findByClassIdAndSectionNameAndSubjectId(Long classId, String sectionName, Long subjectId);
}
