package com.school.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "classes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@EntityListeners(AuditingEntityListener.class)
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId;
    
    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", updatable = false)
    @CreatedBy
    private String createdBy;
    
    @Column(name = "updated_by", nullable = false)
    @LastModifiedBy
    private String updatedBy;

}
