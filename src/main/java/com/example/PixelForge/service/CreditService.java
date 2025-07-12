package com.example.PixelForge.service;

import com.example.PixelForge.InsufficientCreditsException;
import com.example.PixelForge.entity.Credit;
import com.example.PixelForge.entity.CreditLog;
import com.example.PixelForge.entity.Feature;
import com.example.PixelForge.entity.User;
import com.example.PixelForge.repository.CreditLogRepository;
import com.example.PixelForge.repository.CreditRepository;
import com.example.PixelForge.repository.FeatureRepository;
import com.example.PixelForge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRepository creditRepository;
    private final FeatureRepository featureRepository;
    private final CreditLogRepository creditLogRepository;
    private final UserRepository userRepository;

    public void initializeCreditsForNewUser(UUID userId) {
        Credit credit = Credit.builder()
                .userId(userId)
                .balance(50) // Free credits
                .updatedAt(LocalDateTime.now())
                .build();
        creditRepository.save(credit);
    }

    public void deductCredits(UUID userId, String modelName) {
        Feature feature = featureRepository.findByModelName(modelName)
                .orElseThrow(() -> new RuntimeException("Feature not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Credit credit = creditRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Credit record not found"));

        int cost = feature.getCreditCost();
        if (credit.getBalance() < cost) {
            throw new InsufficientCreditsException();
        }

        credit.setBalance(credit.getBalance() - cost);
        credit.setUpdatedAt(LocalDateTime.now());
        creditRepository.save(credit);

        CreditLog log = CreditLog.builder()
                .userId(userId)
                .modelUsed(modelName)
                .creditsSpent(cost)
                .createdAt(LocalDateTime.now())
                .build();
        creditLogRepository.save(log);
    }
}