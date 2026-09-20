package com.school.academicservice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.academicservice.converter.AttendanceConverter;
import com.school.academicservice.dto.AttendanceDTO;
import com.school.academicservice.entity.Attendance;
import com.school.academicservice.repository.AttendanceRepository;
import com.school.common.enums.UserRole;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.service.BaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AttendanceService extends BaseService {
	private final AttendanceRepository attendanceRepository;
	private final AttendanceConverter attendanceConverter;

	public void markAttendance(AttendanceDTO attendanceDTO) {
		log.info("Marking attendance for student: {} on date: {}", attendanceDTO.getAdmissionNumber(),
				attendanceDTO.getAttendanceDate());
		markAttendanceForAll(List.of(attendanceDTO));
	}

	public void markAttendanceForAll(List<AttendanceDTO> attendanceDTOs) {
		List<Attendance> attList = new ArrayList<Attendance>();
		for (AttendanceDTO attendanceDTO : attendanceDTOs) {
			Attendance existingAtt = attendanceRepository.findByAdmissionNumberAndAttendanceDate(
					attendanceDTO.getAdmissionNumber(), attendanceDTO.getAttendanceDate()).orElse(null);
			if (existingAtt != null) {
				if(!Objects.equals(existingAtt.getStatus(), attendanceDTO.getStatus())) {
					existingAtt.setStatus(attendanceDTO.getStatus());
					attList.add(existingAtt);
				}
			} else {
				attList.add(attendanceConverter.dtoToEntity(attendanceDTO));
			}
		}
		attendanceRepository.saveAll(attList);
	}

	public AttendanceDTO getAttendanceById(Long id) {
		log.info("Fetching attendance with id: {}", id);
		Attendance attendance = attendanceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));
		return attendanceConverter.entityToDTO(attendance);
	}

	public List<AttendanceDTO> getStudentAttendance(Long admissionNumber, Long classId) {
		log.info("Fetching attendance for student: {}", admissionNumber);
		List<Attendance> attendances = attendanceRepository.findByAdmissionNumberAndClassId(admissionNumber, classId);
		return attendances.stream().map(attendanceConverter::entityToDTO).collect(Collectors.toList());
	}

	public List<AttendanceDTO> getStudentAttendanceBetweenDates(Long admissionNumber, LocalDate fromDate,
			LocalDate toDate) {
		log.info("Fetching attendance for student: {} between {} and {}", admissionNumber, fromDate, toDate);
		List<Attendance> attendances = attendanceRepository
				.findByAdmissionNumberAndAttendanceDateBetween(admissionNumber, fromDate, toDate);
		return attendances.stream().map(attendanceConverter::entityToDTO).collect(Collectors.toList());
	}

	public List<AttendanceDTO> getStudentAttendanceBetweenDates(LocalDate fromDate, LocalDate toDate) {

		String loginUser = getCurrentUsername();
		Long admissionNumber = loginUser != null && hasRole(UserRole.STUDENT) ? Long.parseLong(loginUser) : null;

		log.info("Fetching attendance for student: {} between {} and {}", admissionNumber, fromDate, toDate);
		return getStudentAttendanceBetweenDates(admissionNumber, fromDate, toDate);
	}

	public List<AttendanceDTO> getStudentAttendanceBetweenDates(Long classId, String sectionName, Long admissionNumber, LocalDate fromDate,
			LocalDate toDate) {
		log.info("Fetching attendance for class: {} section: {} between {} and {}", classId, sectionName, fromDate, toDate);
		List<Attendance> attendances = new ArrayList<>();
		if(admissionNumber != null) {
			attendances = attendanceRepository
					.findByAdmissionNumberAndAttendanceDateBetween(admissionNumber, fromDate, toDate);
		} else {
			if(fromDate != null && toDate != null) {
				attendances = attendanceRepository
						.findByClassIdAndSectionNameAndAttendanceDateBetween(classId, sectionName, fromDate, toDate);
			}else {
			attendances = attendanceRepository
					.findByClassIdAndSectionNameAndAttendanceDate(classId, sectionName, fromDate);
			}
		}
		
		return attendances.stream().map(attendanceConverter::entityToDTO).collect(Collectors.toList());
	}

	public List<AttendanceDTO> getClassSectionAttendance(Long classId, String sectionName, LocalDate attendanceDate) {
		log.info("Fetching attendance for class: {} section: {} on date: {}", classId, sectionName, attendanceDate);
		List<Attendance> attendances = attendanceRepository.findByClassIdAndSectionNameAndAttendanceDate(classId,
				sectionName, attendanceDate);
		return attendances.stream().map(attendanceConverter::entityToDTO).collect(Collectors.toList());
	}

	public AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO) {
		log.info("Updating attendance with id: {}", id);
		Attendance attendance = attendanceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

		attendance.setStatus(attendanceDTO.getStatus());
		attendance.setRemarks(attendanceDTO.getRemarks());

		attendance = attendanceRepository.save(attendance);
		log.info("Attendance updated successfully with id: {}", attendance.getId());
		return attendanceConverter.entityToDTO(attendance);
	}

	public void deleteAttendance(Long id) {
		log.info("Deleting attendance with id: {}", id);
		if (!attendanceRepository.existsById(id)) {
			throw new ResourceNotFoundException("Attendance", "id", id);
		}
		attendanceRepository.deleteById(id);
		log.info("Attendance deleted successfully with id: {}", id);
	}
}
