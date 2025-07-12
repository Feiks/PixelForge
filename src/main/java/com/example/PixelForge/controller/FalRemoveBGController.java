package com.example.PixelForge.controller;

import com.example.PixelForge.service.CreditService;
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
@RequestMapping("/api/fal/queue")
@RequiredArgsConstructor
public class FalRemoveBGController {

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final CreditService creditService;

    @PostMapping("/remove-bg")
    public ResponseEntity<?> submitBackgroundRemoval(@RequestBody Map<String, String> body,
                                                     HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing userId");
        }

        String imageUrl = body.get("imageUrl");
        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'imageUrl'");
        }

        // ✅ Deduct credits
        creditService.deductCredits(userId, "fal-ai/bria/background/remove");

        String submitUrl = "https://queue.fal.run/fal-ai/bria/background/remove";

        JsonObject payload = new JsonObject();
        payload.addProperty("image_url", imageUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(submitUrl, entity, String.class);

        return ResponseEntity.ok(response.getBody()); // Contains request_id
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<String> checkStatus(@PathVariable String requestId) {
        String statusUrl = "https://queue.fal.run/fal-ai/bria/requests/" + requestId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(statusUrl, HttpMethod.GET, entity, String.class);

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/result/{requestId}")
    public ResponseEntity<String> getResult(@PathVariable String requestId) {
        String resultUrl = "https://queue.fal.run/fal-ai/bria/requests/" + requestId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(resultUrl, HttpMethod.GET, entity, String.class);

        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
        if (json.has("image")) {
            String finalImage = json.getAsJsonObject("image").get("url").getAsString();
            return ResponseEntity.ok(finalImage);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Result not ready yet or failed.");
    }
}
