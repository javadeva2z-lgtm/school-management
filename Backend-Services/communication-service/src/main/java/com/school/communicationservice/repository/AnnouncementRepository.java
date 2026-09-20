package com.school.communicationservice.repository;

import com.school.communicationservice.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
	List<Announcement> findByActiveTrueOrderByPostedDateDesc();

	List<Announcement> findByClassIdAndActiveTrueOrderByPostedDateDesc(Long classId);
}
