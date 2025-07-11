package com.example.PixelForge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Feature {
    @Id
    private String modelName; // Primary key: fal-ai/object-removal

    private String title;     // "Object Removal"
    private int creditCost;   // e.g., 5
}
