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
@Table(name = "credits")
public class Credit {
    @Id
    private UUID userId;

    private int balance;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
