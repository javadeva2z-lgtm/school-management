package com.school.communicationservice.repository;

import com.school.communicationservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
	List<Event> findByActiveTrueAndEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);
}
