package com.codesync.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

import static com.codesync.execution.ExecutionDtos.*;

/**
 * Wraps the Judge0 CE API (https://judge0.com). Uses the free public
 * instance at ce.judge0.com, which requires no API key/card — good for
 * a portfolio project. It has modest rate limits, so if you outgrow it
 * later, swap in a RapidAPI key or a self-hosted instance (the base URL
 * and headers are the only things that change).
 */
@Component
public class Judge0Client {

    @Value("${app.judge0.base-url}")
    private String baseUrl;

    @Value("${app.judge0.api-key:}")
    private String apiKey;

    @Value("${app.judge0.api-host:}")
    private String apiHost;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Judge0Client(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    private static final Map<String, Integer> LANGUAGE_IDS = Map.of(
            "java", 62,       // Java (OpenJDK 13.0.1)
            "python", 71,     // Python 3.8.1
            "javascript", 63  // JavaScript (Node.js 12.14.0)
    );

    public ExecuteResponse execute(ExecuteRequest request) {
        Integer languageId = LANGUAGE_IDS.get(request.language().toLowerCase());
        if (languageId == null) {
            throw new IllegalArgumentException("Unsupported language: " + request.language());
        }

        Map<String, Object> body = Map.of(
                "source_code", Base64.getEncoder().encodeToString(request.code().getBytes()),
                "language_id", languageId,
                "stdin", Base64.getEncoder().encodeToString(
                        (request.stdin() != null ? request.stdin() : "").getBytes())
        );

        WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(baseUrl + "/submissions?base64_encoded=true&wait=true&fields=stdout,stderr,status,time")
                .header("Content-Type", "application/json");

        // Only attach RapidAPI headers if a key is actually configured —
        // the free ce.judge0.com public instance needs none of this.
        if (apiKey != null && !apiKey.isBlank()) {
            requestSpec = requestSpec
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost);
        }

        JsonNode result = requestSpec
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (result == null) {
            return new ExecuteResponse("", "No response from execution service", "ERROR", null);
        }

        String stdout = decode(result.path("stdout").asText(null));
        String stderr = decode(result.path("stderr").asText(null));
        String status = result.path("status").path("description").asText("UNKNOWN");
        Double time = result.path("time").isMissingNode() ? null : result.path("time").asDouble();

        return new ExecuteResponse(stdout, stderr, status, time);
    }

    private String decode(String base64) {
        if (base64 == null) return "";
        try {
            return new String(Base64.getDecoder().decode(base64));
        } catch (IllegalArgumentException e) {
            return base64; // wasn't actually base64 (e.g. already plain text)
        }
    }
}