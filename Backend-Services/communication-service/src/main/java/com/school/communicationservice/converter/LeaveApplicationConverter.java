package com.school.communicationservice.converter;

import com.school.communicationservice.dto.LeaveApplicationDTO;
import com.school.communicationservice.entity.LeaveApplication;
import org.springframework.stereotype.Component;

@Component
public class LeaveApplicationConverter {
	public LeaveApplicationDTO toDto(LeaveApplication e) {
		return LeaveApplicationDTO.builder().id(e.getId()).admissionNumber(e.getAdmissionNumber()).reason(e.getReason())
				.fromDate(e.getFromDate()).toDate(e.getToDate()).totalDays(e.getTotalDays()).status(e.getStatus())
				.approvedBy(e.getApprovedBy()).approvalDate(e.getApprovalDate()).remarks(e.getRemarks()).build();
	}

	public LeaveApplication toEntity(LeaveApplicationDTO d) {
		return LeaveApplication.builder().id(d.getId()).admissionNumber(d.getAdmissionNumber()).reason(d.getReason())
				.fromDate(d.getFromDate()).toDate(d.getToDate()).totalDays(d.getTotalDays()).status(d.getStatus())
				.approvedBy(d.getApprovedBy()).approvalDate(d.getApprovalDate()).remarks(d.getRemarks()).build();
	}
}
