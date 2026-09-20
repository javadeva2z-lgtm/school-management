package com.school.utilityservice.repository;

import com.school.utilityservice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<FileMetadata> findByUploadedByOrderByCreatedAtDesc(Long uploadedBy);
}
