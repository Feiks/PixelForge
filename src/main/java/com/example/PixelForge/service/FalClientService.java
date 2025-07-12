package com.example.PixelForge.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class FalClientService {

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendToFal(String modelName, String imageUrl, String prompt) {
        String endpoint = "https://queue.fal.run/" + modelName;

        JsonObject payload = new JsonObject();
        payload.addProperty("image_url", imageUrl);
        if (prompt != null) payload.addProperty("prompt", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
        return json.get("request_id").getAsString();
    }

    public String checkStatus(String requestId) {
        String endpoint = "https://queue.fal.run/fal-ai/image-editing/requests/" + requestId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
        return json.get("status").getAsString();
    }

    public String getResultUrl(String requestId) {
        String endpoint = "https://queue.fal.run/fal-ai/image-editing/requests/" + requestId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();

        JsonArray images = json.getAsJsonArray("images");
        if (images != null && images.size() > 0) {
            return images.get(0).getAsJsonObject().get("url").getAsString();
        }
        return null;
    }
}
