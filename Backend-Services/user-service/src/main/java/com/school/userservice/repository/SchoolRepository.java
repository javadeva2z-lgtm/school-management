package com.school.userservice.repository;

import com.school.userservice.entity.School;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findBySchoolCode(String schoolCode);
    List<School> findByIsActiveTrue();
    Optional<School> findByKeywords(String keywords);
}

