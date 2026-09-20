package com.school.communicationservice.converter;

import com.school.communicationservice.dto.AnnouncementDTO;
import com.school.communicationservice.entity.Announcement;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementConverter {
	public AnnouncementDTO toDto(Announcement e) {
		return AnnouncementDTO.builder().id(e.getId()).createdBy(e.getCreatedBy()).title(e.getTitle())
				.content(e.getContent()).fileUrl(e.getFileUrl()).classId(e.getClassId()).sectionName(e.getSectionName())
				.postedDate(e.getPostedDate()).expiresDate(e.getExpiresDate()).active(e.getActive()).build();
	}

	public Announcement toEntity(AnnouncementDTO d) {
		return Announcement.builder().id(d.getId()).createdBy(d.getCreatedBy()).title(d.getTitle())
				.content(d.getContent()).fileUrl(d.getFileUrl()).classId(d.getClassId()).sectionName(d.getSectionName())
				.postedDate(d.getPostedDate()).expiresDate(d.getExpiresDate()).active(d.getActive()).build();
	}
}
