package com.example.PixelForge.service;

import com.example.PixelForge.entity.ImageJob;
import com.example.PixelForge.repository.ImageJobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImageJobService {

    private final ImageJobRepository imageJobRepository;

    public ImageJobService(ImageJobRepository imageJobRepository) {
        this.imageJobRepository = imageJobRepository;
    }

    public ImageJob save(ImageJob job) {
        return imageJobRepository.save(job);
    }

    public Optional<ImageJob> findById(Long id) {
        return imageJobRepository.findById(id);
    }

    public List<ImageJob> findAll() {
        return imageJobRepository.findAll();
    }

    public void deleteById(Long id) {
        imageJobRepository.deleteById(id);
    }
}