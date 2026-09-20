package com.school.communicationservice.converter;

import com.school.communicationservice.dto.EventDTO;
import com.school.communicationservice.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventConverter {
	public EventDTO toDto(Event e) {
		return EventDTO.builder().id(e.getId()).createdBy(e.getCreatedBy()).eventName(e.getEventName())
				.description(e.getDescription()).eventDate(e.getEventDate()).startTime(e.getStartTime())
				.endTime(e.getEndTime()).location(e.getLocation()).fileUrl(e.getFileUrl()).videoUrl(e.getVideoUrl())
				.classId(e.getClassId()).active(e.getActive()).build();
	}

	public Event toEntity(EventDTO d) {
		return Event.builder().id(d.getId()).createdBy(d.getCreatedBy()).eventName(d.getEventName())
				.description(d.getDescription()).eventDate(d.getEventDate()).startTime(d.getStartTime())
				.endTime(d.getEndTime()).location(d.getLocation()).fileUrl(d.getFileUrl()).videoUrl(d.getVideoUrl())
				.classId(d.getClassId()).active(d.getActive()).build();
	}
}
