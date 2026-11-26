package com.jobpilot.recruiter.integration;

import tools.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class SerpApiClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.serpapi.api-key:}")
    private String apiKey;

    @Value("${app.serpapi.base-url:https://serpapi.com/search.json}")
    private String baseUrl;

    public JsonNode search(String query) {
        return searchWithEngine("google", query);
    }

    public JsonNode searchGoogleJobs(String query) {
        return searchWithEngine("google_jobs", query);
    }

    private JsonNode searchWithEngine(String engine, String query) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            throw new IllegalStateException(
                    "SerpAPI is not configured. Set SERPAPI_API_KEY.");
        }

        try {
            JsonNode response = restClientBuilder.build()
                    .get()
                    .uri(baseUrl + "?engine={engine}&q={query}&api_key={apiKey}&num=10",
                            engine, query, apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new SerpApiException(502, "SerpAPI returned an empty response.");
            }
            if (response.has("error")) {
                throw new SerpApiException(
                        502, "SerpAPI search failed: " + response.path("error").asText());
            }
            return response;

        } catch (RestClientResponseException exception) {
            int statusCode = exception.getStatusCode().value();
            String message = switch (statusCode) {
                case 401, 403 ->
                        "SerpAPI authentication failed. Check your API key or account access.";
                case 429 -> "SerpAPI usage limit exceeded.";
                default -> statusCode >= 500
                        ? "SerpAPI service is temporarily unavailable."
                        : "SerpAPI search request failed.";
            };
            throw new SerpApiException(statusCode, message);
        }
    }
}
