package com.school.paymentservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.paymentservice.entity.MonthlyFee;

@Repository
public interface MonthlyFeeRepository extends JpaRepository<MonthlyFee, Long> {
    Optional<MonthlyFee> findByAdmissionNumberAndMonthYear(Long admissionNumber, String monthYear);

    List<MonthlyFee> findByAdmissionNumber(Long admissionNumber);
}