package com.school.utilityservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FileMetadataDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String bucketName;
    private String objectName;
    private Long uploadedBy;
    private String entityType;
    private Long entityId;
    private LocalDateTime createdAt;
}
