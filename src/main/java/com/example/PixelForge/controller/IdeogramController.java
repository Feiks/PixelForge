package com.example.PixelForge.controller;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/ideogram")
public class IdeogramController {

    @Value("${replicate.api.token}")
    private String replicateApiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/generate")
    public ResponseEntity<String> generateImage(@RequestBody Map<String, String> body) throws InterruptedException {
        String prompt = body.get("prompt");
        if (prompt == null) {
            return ResponseEntity.badRequest().body("Missing 'prompt'");
        }

        // Step 1: Submit generation request
        String submitUrl = "https://api.replicate.com/v1/predictions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + replicateApiToken);

        Map<String, Object> input = Map.of(
                "prompt", prompt,
                "aspect_ratio", "16:9"
        );

        Map<String, Object> payload = Map.of(
                "version", "e78d8251b33015f3d743e83e8631b6cf53fed99057e4ddbaa6ee52340b4c2910",
                "input", input
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> submissionResponse = restTemplate.postForEntity(submitUrl, request, String.class);
        if (!submissionResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(submissionResponse.getStatusCode()).body("Error starting generation");
        }

        JsonObject submissionJson = JsonParser.parseString(submissionResponse.getBody()).getAsJsonObject();
        String predictionId = submissionJson.get("id").getAsString();

        String getUrl = "https://api.replicate.com/v1/predictions/" + predictionId;

        while (true) {
            HttpEntity<Void> getRequest = new HttpEntity<>(headers);
            ResponseEntity<String> pollResponse = restTemplate.exchange(getUrl, HttpMethod.GET, getRequest, String.class);
            JsonObject pollJson = JsonParser.parseString(pollResponse.getBody()).getAsJsonObject();
            String status = pollJson.get("status").getAsString();

            if (status.equals("succeeded")) {
                JsonElement outputElement = pollJson.get("output");

                if (outputElement != null && outputElement.isJsonArray()) {
                    JsonArray outputArray = outputElement.getAsJsonArray();
                    String imageUrl = outputArray.get(0).getAsString();
                    return ResponseEntity.ok(imageUrl);
                } else if (outputElement != null && outputElement.isJsonPrimitive()) {
                    String imageUrl = outputElement.getAsString();
                    return ResponseEntity.ok(imageUrl);
                } else {
                    return ResponseEntity.status(500).body("No output found in response.");
                }

            } else if (status.equals("failed")) {
                return ResponseEntity.status(500).body("Generation failed.");
            }

            Thread.sleep(2000);
        }
    }
}
