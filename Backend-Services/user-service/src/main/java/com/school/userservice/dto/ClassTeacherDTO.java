package com.school.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassTeacherDTO {
    private Long id;
    private Long classId;
    private String sectionName;
    private Long teacherId;    
}
