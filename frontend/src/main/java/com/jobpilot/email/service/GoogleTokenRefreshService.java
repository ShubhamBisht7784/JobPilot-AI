
package com.jobpilot.email.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobpilot.email.entity.GmailConnection;
import com.jobpilot.email.repository.GmailConnectionRepository;


@Service
public class GoogleTokenRefreshService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final TokenEncryptionService tokenEncryptionService;
    private final RestClient.Builder restClientBuilder;

    public GoogleTokenRefreshService(GmailConnectionRepository gmailConnectionRepository,
            TokenEncryptionService tokenEncryptionService, RestClient.Builder restClientBuilder) {
        this.gmailConnectionRepository = gmailConnectionRepository;
        this.tokenEncryptionService = tokenEncryptionService;
        this.restClientBuilder = restClientBuilder;
    }

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String clientSecret;

    @Transactional
    public String refreshAccessToken(GmailConnection connection) {

        if (connection.getRefreshToken() == null ||
                connection.getRefreshToken().isBlank()) {
            throw new IllegalStateException("Refresh token is unavailable.");
        }

        String refreshToken = tokenEncryptionService.decrypt(
                connection.getRefreshToken()
        );

        TokenResponse response = restClientBuilder.build()
                .post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new org.springframework.util.LinkedMultiValueMap<>(
                        Map.of(
                                "client_id", java.util.List.of(clientId),
                                "client_secret", java.util.List.of(clientSecret),
                                "refresh_token", java.util.List.of(refreshToken),
                                "grant_type", java.util.List.of("refresh_token")
                        )
                ))
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.getAccessToken() == null
                || response.getExpiresIn() == null || response.getExpiresIn() <= 0) {
            throw new IllegalStateException("Google returned an invalid token response.");
        }

        connection.setAccessToken(
                tokenEncryptionService.encrypt(response.getAccessToken())
        );

        connection.setAccessTokenExpiresAt(
                Instant.now().plusSeconds(response.getExpiresIn())
        );

        gmailConnectionRepository.save(connection);

        return response.getAccessToken();
    }

    public static class TokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private Long expiresIn;

        @JsonProperty("token_type")
        private String tokenType;
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String value) { accessToken = value; }
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long value) { expiresIn = value; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String value) { tokenType = value; }
    }
}
