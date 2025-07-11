package com.example.PixelForge.service;

import com.example.PixelForge.entity.Credit;
import com.example.PixelForge.entity.ImageJob;
import com.example.PixelForge.entity.User;
import com.example.PixelForge.repository.CreditRepository;
import com.example.PixelForge.repository.ImageJobRepository;
import com.example.PixelForge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final ImageJobRepository imageJobRepository;
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final FalClientService falClientService;

    private final int COST_PER_GENERATION = 1;

    @Transactional
    public ImageJob createJob(String modelName, String inputImageUrl, String prompt, UUID userId) {
        // 1. Fetch user and credits
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Credit credit = creditRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Credit record not found"));

        if (credit.getBalance() < COST_PER_GENERATION) {
            throw new RuntimeException("Insufficient credits");
        }

        ImageJob job = new ImageJob();
        job.setUser(user);
        job.setInputImageUrl(inputImageUrl);
        job.setModelName(modelName);
        job.setPrompt(prompt);
        job.setCreatedAt(LocalDateTime.now());
        job.setStatus(ImageJob.JobStatus.PROCESSING);
        imageJobRepository.save(job);

        String requestId = falClientService.sendToFal(modelName, inputImageUrl, prompt);
        job.setFalRequestId(requestId);
        imageJobRepository.save(job);

        credit.setBalance(credit.getBalance() - COST_PER_GENERATION);
        creditRepository.save(credit);

        return job;
    }

    public Optional<ImageJob> checkStatus(Long jobId) {
        Optional<ImageJob> jobOpt = imageJobRepository.findById(jobId);
        jobOpt.ifPresent(job -> {
            if ("PENDING".equals(job.getStatus())) {
                String status = falClientService.checkStatus(job.getFalRequestId());
                if ("COMPLETED".equals(status)) {
                    String resultUrl = falClientService.getResultUrl(job.getFalRequestId());
                    job.setOutputImageUrl(resultUrl);
                    job.setStatus(ImageJob.JobStatus.SUBMITTED);
                    job.setCompletedAt(LocalDateTime.now());
                    imageJobRepository.save(job);
                }
            }
        });
        return jobOpt;
    }

    public int getUserCreditBalance(UUID userId) {

        Credit credit = creditRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Credit not found"));
        return credit.getBalance();
    }
}
