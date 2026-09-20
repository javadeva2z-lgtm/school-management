package com.school.utilityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileMetadata {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(name = "file_path", nullable = false, length = 500) private String filePath;
    @Column(name = "file_size", nullable = false) private Long fileSize;
    @Column(name = "file_type", length = 50) private String fileType;
    @Column(name = "bucket_name") private String bucketName;
    @Column(name = "object_name", length = 500) private String objectName;
    @Column(name = "uploaded_by", nullable = false) private Long uploadedBy;
    @Column(name = "entity_type", length = 50) private String entityType;
    @Column(name = "entity_id") private Long entityId;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
