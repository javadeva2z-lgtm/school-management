package com.school.communicationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDTO {
	private Long id;
	@NotNull
	private Long createdBy;
	@NotBlank
	private String title;
	@NotBlank
	private String content;
	private String fileUrl;
	private Long classId;
	private String sectionName;
	private LocalDateTime postedDate;
	private LocalDateTime expiresDate;
	private Boolean active;
}
