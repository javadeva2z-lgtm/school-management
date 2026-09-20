package com.school.academicservice.repository;

import com.school.academicservice.entity.HomeworkFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeworkFileRepository extends JpaRepository<HomeworkFile, Long> {
    List<HomeworkFile> findByHomeworkIdOrderById(Long homeworkId);
}
