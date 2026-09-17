package com.mediqai.ai.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    public String generateContent(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        Map<String, Object> response = restClient.post()
                .uri("/v1beta/models/gemini-3.6-flash:generateContent")
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("Gemini không trả về dữ liệu");
        }

        try {
            var candidates = (java.util.List<Map<String, Object>>)
                    response.get("candidates");

            var content = (Map<String, Object>)
                    candidates.get(0).get("content");

            var parts = (java.util.List<Map<String, Object>>)
                    content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Không thể đọc response từ Gemini", e
            );
        }
    }
}