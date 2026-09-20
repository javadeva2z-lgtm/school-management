package com.school.paymentservice.repository;

import com.school.paymentservice.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findByClassIdAndAcademicYear(Long classId, String academicYear);
    Optional<FeeStructure> findByClassIdAndFeeTypeAndAcademicYear(Long classId, String feeType, String academicYear);
    List<FeeStructure> findByClassId(Long classId);
    List<FeeStructure> findByIsActiveTrue();
}
