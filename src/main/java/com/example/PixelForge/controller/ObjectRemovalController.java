package com.example.PixelForge.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/fal/object-removal")
public class ObjectRemovalController {

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Submit object removal request
    @PostMapping("/submit")
    public ResponseEntity<String> submit(@RequestBody Map<String, Object> body) {
        String imageUrl = (String) body.get("image_url");
        String prompt = (String) body.get("prompt");
        String model = (String) body.getOrDefault("model", "best_quality");
        Integer maskExpansion = (Integer) body.getOrDefault("mask_expansion", 15);

        if (imageUrl == null || prompt == null) {
            return ResponseEntity.badRequest().body("Missing 'image_url' or 'prompt'");
        }

        String endpoint = "https://queue.fal.run/fal-ai/object-removal";

        JsonObject payload = new JsonObject();
        payload.addProperty("image_url", imageUrl);
        payload.addProperty("prompt", prompt);
        payload.addProperty("model", model);
        payload.addProperty("mask_expansion", maskExpansion);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

        return ResponseEntity.ok(response.getBody()); // Contains request_id
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
