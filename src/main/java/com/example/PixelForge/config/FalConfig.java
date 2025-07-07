package com.example.PixelForge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fal.api")
@Data
public class FalConfig {
    private String key;
    private String baseUrl;
}
