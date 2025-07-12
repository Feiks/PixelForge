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
@RequestMapping("/api/fal/text-removal")
@RequiredArgsConstructor
public class TextRemovalController {

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final CreditService creditService;

    @PostMapping("/submit")
    public ResponseEntity<String> submit(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing userId");
        }

        String imageUrl = (String) body.get("image_url");
        Double guidanceScale = body.get("guidance_scale") != null ? (Double) body.get("guidance_scale") : 3.5;
        Integer steps = body.get("num_inference_steps") != null ? (Integer) body.get("num_inference_steps") : 30;
        String safety = (String) body.getOrDefault("safety_tolerance", "2");
        String format = (String) body.getOrDefault("output_format", "jpeg");

        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'image_url'");
        }

        creditService.deductCredits(userId, "fal-ai/image-editing/text-removal");

        JsonObject payload = new JsonObject();
        payload.addProperty("image_url", imageUrl);
        payload.addProperty("guidance_scale", guidanceScale);
        payload.addProperty("num_inference_steps", steps);
        payload.addProperty("safety_tolerance", safety);
        payload.addProperty("output_format", format);

        String endpoint = "https://queue.fal.run/fal-ai/image-editing/text-removal";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<String> checkStatus(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/image-editing/requests/" + requestId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @GetMapping("/result/{requestId}")
    public ResponseEntity<String> getResult(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/image-editing/requests/" + requestId;

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
