package com.example.PixelForge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ImageJob.java
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
public class ImageJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String model;
    private String prompt;
    private String imageUrl;
    private String resultUrl;
    private String inputImageUrl;
    private String modelName;
    private String falRequestId;
    private String outputImageUrl;
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum JobStatus {
        SUBMITTED, PROCESSING, COMPLETED, FAILED
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = JobStatus.SUBMITTED;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
