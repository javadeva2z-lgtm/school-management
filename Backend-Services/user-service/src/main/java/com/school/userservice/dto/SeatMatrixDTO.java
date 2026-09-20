package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatMatrixDTO {
	private Long classId;
	private String sectionName;
	private int totalCapacity;
	private int occupiedCapacity;
	private int vacantSeats;

}
