package com.school.academicservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "results")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_number", nullable = false)
    private Long admissionNumber;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "exam_schedule_id", nullable = false)
    private Long examScheduleId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "marks_obtained", nullable = false)
    private Integer marksObtained;

    @Column(name = "out_of", nullable = false)
    @Builder.Default
    private Integer outOf = 100;

    @Column(length = 10)
    private String grade;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate 
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate 
    private LocalDateTime updatedAt;
}
