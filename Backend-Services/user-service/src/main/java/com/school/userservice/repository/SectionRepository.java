package com.school.userservice.repository;

import com.school.userservice.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByClassIdAndSectionName(Long classId, String sectionName);
    List<Section> findByClassId(Long classId);
    List<Section> findByClassIdAndIsActiveTrue(Long classId);
}
