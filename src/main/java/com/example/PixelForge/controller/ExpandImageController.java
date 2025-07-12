package com.example.PixelForge.controller;

import com.example.PixelForge.service.CreditService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fal/reframe")
@RequiredArgsConstructor
public class ExpandImageController {

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final CreditService creditService;

    // Step 1: Submit Reframe Request
    @PostMapping("/submit")
    public ResponseEntity<?> submitReframe(@RequestBody Map<String, String> body,
                                           HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing userId");
        }

        String imageUrl = body.get("image_url");
        String aspectRatio = body.getOrDefault("aspect_ratio", "16:9");

        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'image_url'");
        }

        // ✅ Deduct credits
        creditService.deductCredits(userId, "fal-ai/luma-photon/flash/reframe");

        String endpoint = "https://queue.fal.run/fal-ai/luma-photon/flash/reframe";

        JsonObject payload = new JsonObject();
        payload.addProperty("image_url", imageUrl);
        payload.addProperty("aspect_ratio", aspectRatio);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);

        return ResponseEntity.ok(response.getBody());
    }

    // Step 2: Check status
    @GetMapping("/status/{requestId}")
    public ResponseEntity<String> checkStatus(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/luma-photon/requests/" + requestId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    // Step 3: Get final result
    @GetMapping("/result/{requestId}")
    public ResponseEntity<String> getResult(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/luma-photon/requests/" + requestId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();

        if (json.has("images")) {
            JsonArray images = json.getAsJsonArray("images");
            if (images.size() > 0) {
                String imageUrl = images.get(0).getAsJsonObject().get("url").getAsString();
                return ResponseEntity.ok(imageUrl);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not available yet.");
    }
}
