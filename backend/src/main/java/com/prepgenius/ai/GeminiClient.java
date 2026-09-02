package com.prepgenius.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient implements GeminiClientInterface {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper;

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String getModel() {
        return model;
    }

    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is not configured.");
        }

        String url = baseUrl + model + ":generateContent?key=" + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        try {
            RestClient restClient = RestClient.create();
            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("candidates")) {
                throw new RuntimeException("Invalid response from Gemini API");
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates.isEmpty()) {
                throw new RuntimeException("No candidates found in Gemini response");
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts.isEmpty()) {
                throw new RuntimeException("No parts found in Gemini response content");
            }

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Error communicating with Gemini API", e);
            throw new RuntimeException("AI service is temporarily unavailable: " + e.getMessage());
        }
    }
}
