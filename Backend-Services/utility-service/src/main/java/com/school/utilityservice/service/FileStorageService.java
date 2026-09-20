package com.school.utilityservice.service;

import com.google.cloud.storage.*;
import com.school.utilityservice.dto.FileMetadataDTO;
import com.school.utilityservice.entity.FileMetadata;
import com.school.utilityservice.repository.FileMetadataRepository;
import com.school.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class FileStorageService {
    private final Storage storage;
    private final FileMetadataRepository repository;
    @Value("${app.storage.bucket}") private String bucket;

    public FileMetadataDTO upload(MultipartFile file, Long uploadedBy, String entityType, Long entityId) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("File must not be empty");
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file").replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectName = UUID.randomUUID() + "-" + originalName;
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, objectName)).setContentType(file.getContentType()).build();
        storage.create(blobInfo, file.getBytes());
        FileMetadata metadata = repository.save(FileMetadata.builder().fileName(originalName).filePath("gs://" + bucket + "/" + objectName).fileSize(file.getSize()).fileType(file.getContentType()).bucketName(bucket).objectName(objectName).uploadedBy(uploadedBy).entityType(entityType).entityId(entityId).build());
        return toDto(metadata);
    }

    @Transactional(readOnly = true)
    public byte[] download(Long id) {
        FileMetadata metadata = find(id);
        Blob blob = storage.get(BlobId.of(metadata.getBucketName(), metadata.getObjectName()));
        if (blob == null) throw new ResourceNotFoundException("File object not found");
        return blob.getContent();
    }

    @Transactional(readOnly = true) public FileMetadataDTO metadata(Long id) { return toDto(find(id)); }
    @Transactional(readOnly = true) public List<FileMetadataDTO> byUploader(Long uploadedBy) { return repository.findByUploadedByOrderByCreatedAtDesc(uploadedBy).stream().map(this::toDto).toList(); }
    public void delete(Long id) { FileMetadata metadata = find(id); storage.delete(BlobId.of(metadata.getBucketName(), metadata.getObjectName())); repository.delete(metadata); }
    public List<FileMetadataDTO> uploadBulk(List<MultipartFile> files, Long uploadedBy, String entityType, Long entityId) throws IOException { List<FileMetadataDTO> result = new ArrayList<>(); for (MultipartFile file : files) result.add(upload(file, uploadedBy, entityType, entityId)); return result; }

    private FileMetadata find(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File metadata", "id", id)); }
    private FileMetadataDTO toDto(FileMetadata e) { return FileMetadataDTO.builder().id(e.getId()).fileName(e.getFileName()).filePath(e.getFilePath()).fileSize(e.getFileSize()).fileType(e.getFileType()).bucketName(e.getBucketName()).objectName(e.getObjectName()).uploadedBy(e.getUploadedBy()).entityType(e.getEntityType()).entityId(e.getEntityId()).createdAt(e.getCreatedAt()).build(); }
}
