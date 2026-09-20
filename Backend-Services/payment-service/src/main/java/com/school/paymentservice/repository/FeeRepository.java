package com.school.paymentservice.repository;

import com.school.paymentservice.entity.Fee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentId(Long studentId);
    Page<Fee> findByStudentId(Long studentId, Pageable pageable);
    List<Fee> findByStudentIdAndAcademicYear(Long studentId, String academicYear);
    List<Fee> findByStatus(String status);
    List<Fee> findByDueDateAndStatus(LocalDate dueDate, String status);
    List<Fee> findByDueDateBetween(LocalDate fromDate, LocalDate toDate);
    Long countByStudentIdAndStatus(Long studentId, String status);
    Optional<Fee> findByStudentIdAndFeeStructureId(Long studentId, Long feeStructureId);
}
