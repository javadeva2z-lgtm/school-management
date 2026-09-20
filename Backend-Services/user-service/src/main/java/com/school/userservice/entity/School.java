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
@Table(name = "schools")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@EntityListeners(AuditingEntityListener.class)
public class School {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_code", unique = true, nullable = false)
    private String schoolCode;
    
    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "website")
    private String website;

    @Column (name = "principal_name")
    private String principalName;

    @Column(name = "announcement")
    private String announcement;

    @Column(name = "logo")
    private byte[] logo;

    @Column(name = "favicon")
    private byte[] favicon;

    @Column(name = "banner")
    private byte[] banner;

    @Column(name = "keywords")
    private String keywords;


    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
    
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
