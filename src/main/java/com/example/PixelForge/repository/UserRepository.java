package com.example.PixelForge.repository;

import com.example.PixelForge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(UUID userId);
    Optional<User> findByEmail(String email);

}
