package com.school.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.userservice.dto.SeatMatrixDTO;
import com.school.userservice.entity.Section;
import com.school.userservice.entity.Student;
import com.school.userservice.repository.SectionRepository;
import com.school.userservice.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommonService {
	private final SectionRepository sectionRepository;
	private final StudentRepository studentRepository;

	public List<SeatMatrixDTO> getAllSectionsSeatMatrix() {
		List<SeatMatrixDTO> result = new ArrayList<SeatMatrixDTO>();
		log.info("Fetching all sections seat matrix");
		List<Section> allSections = sectionRepository.findAll();
		List<Student> allStudents = studentRepository.findAll();
		allSections.forEach(sec -> {
			int totalOccupied = (int) allStudents.stream().filter(x -> (Objects.equals(x.getClassId(), sec.getClassId())
					&& Objects.equals(x.getSectionName(), sec.getSectionName()))).count();

			result.add(SeatMatrixDTO.builder().sectionName(sec.getSectionName()).classId(sec.getClassId())
					.totalCapacity(sec.getCapacity()).occupiedCapacity(totalOccupied)
					.vacantSeats(sec.getCapacity() - totalOccupied).build());
		});

		return null;
	}
}
