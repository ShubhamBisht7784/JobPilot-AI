package com.jobpilot.recruiter.integration;

import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class HunterApiClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.hunter.api-key}")
    private String apiKey;

    @Value("${app.hunter.base-url}")
    private String baseUrl;

  public JsonNode findEmailByLinkedIn(String linkedinUrl) {

    if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
        throw new IllegalStateException("Hunter is not configured. Set app.hunter.api-key.");
    }

    try {
        return restClientBuilder.build()
                .get()
                .uri(
                        baseUrl + "/email-finder?linkedin_handle={linkedinHandle}&api_key={apiKey}",
                        linkedinUrl,
                        apiKey
                )
                .retrieve()
                .body(JsonNode.class);

    } catch (org.springframework.web.client.RestClientResponseException exception) {

        int statusCode = exception.getStatusCode().value();

        String message = switch (statusCode) {

            case 400 -> "Invalid LinkedIn URL or request parameters.";

            case 401 -> "Hunter API key is invalid.";

            case 403 -> "Hunter API rate limit reached. Wait before retrying.";

            case 404 -> "Requested resource was not found on Hunter.";

            case 429 -> "Hunter API usage quota exceeded.";

            case 451 -> "Hunter cannot process this person's data.";

            default -> statusCode >= 500
                    ? "Hunter service is temporarily unavailable."
                    : "Hunter API request failed.";
        };

        throw new HunterApiException(statusCode, message);
    }
}
}