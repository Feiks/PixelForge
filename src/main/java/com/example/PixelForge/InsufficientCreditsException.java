package com.example.PixelForge;

public class InsufficientCreditsException extends RuntimeException {
    public InsufficientCreditsException() {
        super("Insufficient credits");
    }
}