package com.school.academicservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkFileDTO {
    private Long id;
    private String fileName;
    private String contentType;
    private long fileSize;
    private String filePath;
    private String downloadUrl;
}
