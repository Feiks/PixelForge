package com.example.PixelForge.repository;

import com.example.PixelForge.entity.ImageJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageJobRepository extends JpaRepository<ImageJob, Long> {
}