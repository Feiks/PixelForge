package com.example.PixelForge.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/fal/upscale/crisp")
public class CrispUpscaleController {

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/submit")
    public ResponseEntity<String> submit(@RequestBody Map<String, Object> body) {
        String imageUrl = (String) body.get("image_url");
        if (imageUrl == null) {
            return ResponseEntity.badRequest().body("Missing 'image_url'");
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("image_url", imageUrl);

        String endpoint = "https://queue.fal.run/fal-ai/recraft/upscale/crisp";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/status/{requestId}")
    public ResponseEntity<String> checkStatus(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/recraft/requests/" + requestId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @GetMapping("/result/{requestId}")
    public ResponseEntity<String> getResult(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/recraft/requests/" + requestId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
        if (json.has("image")) {
            String imageUrl = json.getAsJsonObject("image").get("url").getAsString();
            return ResponseEntity.ok(imageUrl);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not ready yet.");
    }
}
