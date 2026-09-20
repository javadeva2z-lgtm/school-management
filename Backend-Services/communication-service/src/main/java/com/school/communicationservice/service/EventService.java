package com.school.communicationservice.service;

import com.school.communicationservice.converter.EventConverter;
import com.school.communicationservice.dto.EventDTO;
import com.school.communicationservice.entity.Event;
import com.school.communicationservice.repository.EventRepository;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
	private final EventRepository repository;
	private final EventConverter converter;

	public EventDTO create(EventDTO dto) {
		return converter.toDto(repository.save(converter.toEntity(dto)));
	}

	@Transactional(readOnly = true)
	public EventDTO get(Long id) {
		return converter.toDto(find(id));
	}

	@Transactional(readOnly = true)
	public List<EventDTO> upcoming(LocalDate from) {
		return repository
				.findByActiveTrueAndEventDateGreaterThanEqualOrderByEventDateAsc(from == null ? LocalDate.now() : from)
				.stream().map(converter::toDto).toList();
	}

	public EventDTO update(Long id, EventDTO dto) {
		Event e = find(id);
		e.setEventName(dto.getEventName());
		e.setDescription(dto.getDescription());
		e.setEventDate(dto.getEventDate());
		e.setStartTime(dto.getStartTime());
		e.setEndTime(dto.getEndTime());
		e.setLocation(dto.getLocation());
		e.setFileUrl(dto.getFileUrl());
		e.setVideoUrl(dto.getVideoUrl());
		e.setClassId(dto.getClassId());
		e.setActive(dto.getActive());
		return converter.toDto(repository.save(e));
	}

	public void delete(Long id) {
		if (!repository.existsById(id))
			throw new ResourceNotFoundException("Event", "id", id);
		repository.deleteById(id);
	}

	private Event find(Long id) {
		return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
	}
}
