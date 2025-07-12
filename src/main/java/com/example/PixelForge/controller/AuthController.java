package com.example.PixelForge.controller;


import com.example.PixelForge.entity.User;
import com.example.PixelForge.service.UserService;
import com.example.PixelForge.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        userService.register(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        User user = userService.authenticate(email, password);
        String accessToken = jwtUtil.generateToken(user.getId(), 15 * 60 * 1000); // 15 min
        String refreshToken = jwtUtil.generateToken(user.getId(), 7 * 24 * 60 * 60 * 1000); // 7 days

        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken
        ));
    }

}
