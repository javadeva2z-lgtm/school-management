package com.school.communicationservice.repository;

import com.school.common.enums.LeaveStatus;
import com.school.communicationservice.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
	List<LeaveApplication> findByAdmissionNumberOrderByCreatedAtDesc(Long admissionNumber);

	List<LeaveApplication> findByStatusOrderByCreatedAtAsc(LeaveStatus status);

	List<LeaveApplication> findByStatusAndApprovedByOrderByCreatedAtAsc(LeaveStatus status, Long approvedBy);
	
	List<LeaveApplication> findByFromDateGreaterThanEqual(LocalDate date);
	
	List<LeaveApplication> findByAdmissionNumberAndFromDateLessThanEqualAndToDateGreaterThanEqual(Long admissionNumber, LocalDate fromDate, LocalDate toDate);

	List<LeaveApplication> findByFromDateLessThanEqualAndToDateGreaterThanEqual(LocalDate fromDate, LocalDate toDate);
}
