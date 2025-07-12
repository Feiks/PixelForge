package com.example.PixelForge.repository;

import com.example.PixelForge.entity.Credit;
import com.example.PixelForge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    Optional<Credit> findByUserId(UUID userId);
}