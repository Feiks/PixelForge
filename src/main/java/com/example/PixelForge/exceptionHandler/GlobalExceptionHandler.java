package com.example.PixelForge.exceptionHandler;

import com.example.PixelForge.InsufficientCreditsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientCreditsException.class)
    public ResponseEntity<?> handleInsufficientCredits(InsufficientCreditsException ex) {
        return ResponseEntity.status(402).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }
}
