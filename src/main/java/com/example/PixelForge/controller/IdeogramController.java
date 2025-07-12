package com.example.PixelForge.controller;

import com.example.PixelForge.service.CreditService;
import com.google.gson.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ideogram")
@RequiredArgsConstructor
public class IdeogramController {

    @Value("${replicate.api.token}")
    private String replicateApiToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final CreditService creditService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateImage(@RequestBody Map<String, String> body,
                                                HttpServletRequest request) throws InterruptedException {
        UUID userId = (UUID) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Missing userId");
        }

        String prompt = body.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body("Missing 'prompt'");
        }

        // ✅ Deduct credit before request
        creditService.deductCredits(userId, "ideogram-ai/ideogram-v2");

        // Step 1: Submit generation request to Replicate
        String submitUrl = "https://api.replicate.com/v1/predictions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + replicateApiToken);

        Map<String, Object> input = Map.of(
                "prompt", prompt,
                "aspect_ratio", "16:9"
        );

        Map<String, Object> payload = Map.of(
                "version", "e78d8251b33015f3d743e83e8631b6cf53fed99057e4ddbaa6ee52340b4c2910", // Replace with latest version if needed
                "input", input
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> submissionResponse = restTemplate.postForEntity(submitUrl, requestEntity, String.class);

        if (!submissionResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(submissionResponse.getStatusCode()).body("Error starting generation");
        }

        JsonObject submissionJson = JsonParser.parseString(submissionResponse.getBody()).getAsJsonObject();
        String predictionId = submissionJson.get("id").getAsString();
        String getUrl = "https://api.replicate.com/v1/predictions/" + predictionId;

        // Step 2: Poll until image is ready
        while (true) {
            HttpEntity<Void> getRequest = new HttpEntity<>(headers);
            ResponseEntity<String> pollResponse = restTemplate.exchange(getUrl, HttpMethod.GET, getRequest, String.class);
            JsonObject pollJson = JsonParser.parseString(pollResponse.getBody()).getAsJsonObject();

            String status = pollJson.get("status").getAsString();
            if (status.equals("succeeded")) {
                JsonElement output = pollJson.get("output");
                if (output != null && output.isJsonArray()) {
                    return ResponseEntity.ok(output.getAsJsonArray().get(0).getAsString());
                } else if (output != null && output.isJsonPrimitive()) {
                    return ResponseEntity.ok(output.getAsString());
                } else {
                    return ResponseEntity.status(500).body("Output missing in Replicate response.");
                }
            } else if (status.equals("failed")) {
                return ResponseEntity.status(500).body("Generation failed.");
            }

            Thread.sleep(2000);
        }
    }
}
