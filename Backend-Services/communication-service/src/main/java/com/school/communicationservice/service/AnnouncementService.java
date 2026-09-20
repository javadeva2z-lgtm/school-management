package com.school.communicationservice.service;

import com.school.communicationservice.converter.AnnouncementConverter;
import com.school.communicationservice.dto.AnnouncementDTO;
import com.school.communicationservice.entity.Announcement;
import com.school.communicationservice.repository.AnnouncementRepository;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {
	private final AnnouncementRepository repository;
	private final AnnouncementConverter converter;

	public AnnouncementDTO create(AnnouncementDTO dto) {
		return converter.toDto(repository.save(converter.toEntity(dto)));
	}

	@Transactional(readOnly = true)
	public AnnouncementDTO get(Long id) {
		return converter.toDto(find(id));
	}

	@Transactional(readOnly = true)
	public List<AnnouncementDTO> active(Long classId) {
		return (classId == null ? repository.findByActiveTrueOrderByPostedDateDesc()
				: repository.findByClassIdAndActiveTrueOrderByPostedDateDesc(classId)).stream().map(converter::toDto)
				.toList();
	}

	public AnnouncementDTO update(Long id, AnnouncementDTO dto) {
		Announcement e = find(id);
		e.setTitle(dto.getTitle());
		e.setContent(dto.getContent());
		e.setFileUrl(dto.getFileUrl());
		e.setClassId(dto.getClassId());
		e.setSectionName(dto.getSectionName());
		e.setExpiresDate(dto.getExpiresDate());
		e.setActive(dto.getActive());
		return converter.toDto(repository.save(e));
	}

	public void delete(Long id) {
		if (!repository.existsById(id))
			throw new ResourceNotFoundException("Announcement", "id", id);
		repository.deleteById(id);
	}

	private Announcement find(Long id) {
		return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
	}
}
