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
@RequestMapping("/api/fal/bg-replace")
public class BgReplaceWithAnotherPictureController {

    @Value("${fal.api.key}")
    private String falApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/submit")
    public ResponseEntity<String> submitBgReplace(@RequestBody Map<String, Object> body) {
        String endpoint = "https://queue.fal.run/fal-ai/bria/background/replace";

        JsonObject payload = new JsonObject();

        payload.addProperty("image_url", (String) body.get("image_url"));

        if (body.containsKey("ref_image_url")) payload.addProperty("ref_image_url", (String) body.get("ref_image_url"));
        if (body.containsKey("prompt")) payload.addProperty("prompt", (String) body.get("prompt"));
        if (body.containsKey("negative_prompt")) payload.addProperty("negative_prompt", (String) body.get("negative_prompt"));
        if (body.containsKey("refine_prompt")) payload.addProperty("refine_prompt", Boolean.parseBoolean(body.get("refine_prompt").toString()));
        if (body.containsKey("fast")) payload.addProperty("fast", Boolean.parseBoolean(body.get("fast").toString()));
        if (body.containsKey("num_images")) payload.addProperty("num_images", Integer.parseInt(body.get("num_images").toString()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

        return ResponseEntity.ok(response.getBody()); // contains request_id
    }

    // STEP 2: Check status
    @GetMapping("/status/{requestId}")
    public ResponseEntity<String> checkStatus(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/bria/requests/" + requestId + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    // STEP 3: Get final result
    @GetMapping("/result/{requestId}")
    public ResponseEntity<String> getResult(@PathVariable String requestId) {
        String url = "https://queue.fal.run/fal-ai/bria/requests/" + requestId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Key " + falApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();

        if (json.has("images")) {
            JsonArray images = json.getAsJsonArray("images");
            if (images.size() > 0) {
                String urlOut = images.get(0).getAsJsonObject().get("url").getAsString();
                return ResponseEntity.ok(urlOut);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not ready yet.");
    }
}
