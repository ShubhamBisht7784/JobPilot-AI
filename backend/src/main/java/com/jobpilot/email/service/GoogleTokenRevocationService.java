
package com.jobpilot.email.service;

import com.jobpilot.email.entity.GmailConnection;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GoogleTokenRevocationService {

    private final TokenEncryptionService tokenEncryptionService;
    private final RestClient.Builder restClientBuilder;

    public void revokeGoogleAccess(GmailConnection connection) {

        String encryptedToken = connection.getRefreshToken();

        // Use access token if refresh token is unavailable
        if (encryptedToken == null || encryptedToken.isBlank()) {
            encryptedToken = connection.getAccessToken();
        }

        if (encryptedToken == null || encryptedToken.isBlank()) {
            throw new IllegalStateException("No Google token available for revocation.");
        }

        String token = tokenEncryptionService.decrypt(encryptedToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", token);

        restClientBuilder.build()
                .post()
                .uri("https://oauth2.googleapis.com/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .toBodilessEntity();
    }
}
