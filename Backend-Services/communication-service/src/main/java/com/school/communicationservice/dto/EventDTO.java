package com.school.communicationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
	private Long id;
	@NotNull
	private Long createdBy;
	@NotBlank
	private String eventName;
	private String description;
	@NotNull
	private LocalDate eventDate;
	private LocalTime startTime;
	private LocalTime endTime;
	private String location;
	private String fileUrl;
	private String videoUrl;
	private Long classId;
	private Boolean active;
}
