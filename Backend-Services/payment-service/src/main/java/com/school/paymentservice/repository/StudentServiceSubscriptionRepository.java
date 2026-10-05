package com.school.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.paymentservice.entity.StudentServiceSubscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentServiceSubscriptionRepository extends JpaRepository<StudentServiceSubscription, Long> {
    List<StudentServiceSubscription> findByAdmissionNumber(Long admissionNumber);

    Optional<StudentServiceSubscription> findByAdmissionNumberAndFeeItemId(Long admissionNumber, Long feeItemId);

    void deleteByFeeItemId(Long feeItemId);
}