package com.school.communicationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Announcement {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "created_by", nullable = false)
	private Long createdBy;
	@Column(nullable = false)
	private String title;
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
	@Column(name = "file_url", length = 500)
	private String fileUrl;
	@Column(name = "class_id")
	private Long classId;
	@Column(name = "section_name")
	private String sectionName;
	@Column(name = "posted_date")
	private LocalDateTime postedDate;
	@Column(name = "expires_date")
	private LocalDateTime expiresDate;
	@Column(name = "is_active")
	private Boolean active;
	@Column(name = "created_at", updatable = false)
	@CreatedDate
	private LocalDateTime createdAt;
	@Column(name = "updated_at")
	@LastModifiedDate
	private LocalDateTime updatedAt;
}
