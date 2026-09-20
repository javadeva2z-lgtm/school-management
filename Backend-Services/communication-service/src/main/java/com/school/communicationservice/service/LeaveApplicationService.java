package com.school.communicationservice.service;

import com.school.communicationservice.converter.LeaveApplicationConverter;
import com.school.communicationservice.dto.LeaveApplicationDTO;
import com.school.communicationservice.entity.LeaveApplication;
import com.school.communicationservice.repository.LeaveApplicationRepository;
import com.school.common.enums.LeaveStatus;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveApplicationService {
	private final LeaveApplicationRepository repository;
	private final LeaveApplicationConverter converter;

	public LeaveApplicationDTO apply(LeaveApplicationDTO dto) {
		validateDates(dto);
		dto.setTotalDays((int) (dto.getToDate().toEpochDay() - dto.getFromDate().toEpochDay() + 1));
		dto.setStatus(LeaveStatus.PENDING);
		return converter.toDto(repository.save(converter.toEntity(dto)));
	}

	@Transactional(readOnly = true)
	public LeaveApplicationDTO get(Long id) {
		return converter.toDto(find(id));
	}

	@Transactional(readOnly = true)
	public List<LeaveApplicationDTO> byStudentAdmissionNumber(Long admissionNumber) {
		return repository.findByAdmissionNumberOrderByCreatedAtDesc(admissionNumber).stream()
				.filter(x -> x.getToDate().isAfter(LocalDate.now().withDayOfYear(1)))
				.map(converter::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LeaveApplicationDTO> byAdmissionNumberAndDate(Long admissionNumber, LocalDate date) {
		return repository.findByAdmissionNumberAndFromDateLessThanEqualAndToDateGreaterThanEqual(admissionNumber, date, date).stream()
				.filter(x -> x.getToDate().isAfter(LocalDate.now().withDayOfYear(1)))
				.map(converter::toDto).toList();
	}
	
	@Transactional(readOnly = true)
	public List<LeaveApplicationDTO> byDate(LocalDate date) {
		return repository.findByFromDateLessThanEqualAndToDateGreaterThanEqual(date, date).stream()
				.map(converter::toDto).toList();
	}

	@Transactional(readOnly = true)
	public List<LeaveApplicationDTO> getCurrentYearLeave() {
		LocalDate firstDayOfCurrentYear = LocalDate.now().withDayOfYear(1);
		return repository.findByFromDateGreaterThanEqual(firstDayOfCurrentYear).stream()
				.filter(x -> x.getToDate().isAfter(LocalDate.now().withDayOfYear(1)))
				.map(converter::toDto).toList();
	}

	@Transactional(readOnly = true)
	public List<LeaveApplicationDTO> byStatus(LeaveStatus status) {
		return repository.findByStatusOrderByCreatedAtAsc(status).stream().map(converter::toDto).toList();
	}

	public LeaveApplicationDTO decide(Long id, LeaveStatus status, String remarks) {
		LeaveApplication e = find(id);
		e.setStatus(status);
		e.setApprovedBy(getLoginUserName());
		e.setApprovalDate(LocalDateTime.now());
		e.setRemarks(remarks);
		return converter.toDto(repository.save(e));
	}

	private void validateDates(LeaveApplicationDTO dto) {
		if (dto.getToDate().isBefore(dto.getFromDate()))
			throw new IllegalArgumentException("toDate must not be before fromDate");
	}

	private LeaveApplication find(Long id) {
		return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave application", "id", id));
	}

	private String getLoginUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
	}
}
