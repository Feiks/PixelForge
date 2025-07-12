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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/fal/object-removal")
public class ObjectRemovalController {

    private final CreditService creditService;

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        System.out.println("userId in controller: " + userId); // ✅ confirm this

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing userId");
        }
        String imageUrl = (String) body.get("image_url");
        String prompt = (String) body.get("prompt");
        String model = (String) body.getOrDefault("model", "best_quality");
        Integer maskExpansion = (Integer) body.getOrDefault("mask_expansion", 15);

        if (imageUrl == null || prompt == null) {
            return ResponseEntity.badRequest().body("Missing 'image_url' or 'prompt'");
        }

        creditService.deductCredits(userId, "fal-ai/object-removal");

        String endpoint = "https://queue.fal.run/fal-ai/object-removal";

        JsonObject payload = new JsonObject();
        payload.addProperty("image_url", imageUrl);
        payload.addProperty("prompt", prompt);
        payload.addProperty("model", model);
        payload.addProperty("mask_expansion", maskExpansion);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> request1 = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request1, String.class);

        return ResponseEntity.ok(response.getBody());
    }

    // Check status
    @GetMapping("/status/{requestId}")
    public ResponseEntity<String> checkStatus(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/object-removal/requests/" + requestId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    // Get result
    @GetMapping("/result/{requestId}")
    public ResponseEntity<String> getResult(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/object-removal/requests/" + requestId;

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

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not ready yet.");
    }
}