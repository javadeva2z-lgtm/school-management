package com.school.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "created_by", nullable = false)
	private Long createdBy;
	@Column(name = "event_name", nullable = false)
	private String eventName;
	@Column(columnDefinition = "TEXT")
	private String description;
	@Column(name = "event_date", nullable = false)
	private LocalDate eventDate;
	@Column(name = "start_time")
	private LocalTime startTime;
	@Column(name = "end_time")
	private LocalTime endTime;
	private String location;
	@Column(name = "file_url", length = 500)
	private String fileUrl;
	@Column(name = "video_url", length = 500)
	private String videoUrl;
	@Column(name = "class_id")
	private Long classId;
	@Column(name = "is_active")
	private Boolean active;
	@Column(name = "created_at", updatable = false)
	@CreatedDate
	private LocalDateTime createdAt;
	@Column(name = "updated_at")
	@LastModifiedDate
	private LocalDateTime updatedAt;
}
