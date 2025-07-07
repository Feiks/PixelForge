package com.example.PixelForge.client;

import com.example.PixelForge.config.FalConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FalApiClient {

    private final FalConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<Map> postToModel(String modelPath, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + config.getKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = config.getBaseUrl() + "/" + modelPath;

        return restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
    }

    public ResponseEntity<Map> getFromFal(String fullUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + config.getKey());

        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(fullUrl, HttpMethod.GET, request, Map.class);
    }
}
