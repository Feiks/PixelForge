package com.example.PixelForge.repository;

import com.example.PixelForge.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeatureRepository  extends JpaRepository<Feature, Integer> {
    Optional<Feature> findByModelName(String modelName);
}
