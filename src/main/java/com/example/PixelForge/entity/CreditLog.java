package com.example.PixelForge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "credit_logs")
public class CreditLog {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    private String modelUsed;

    private int creditsSpent;

    private String resultUrl;

    private LocalDateTime createdAt = LocalDateTime.now();
}
