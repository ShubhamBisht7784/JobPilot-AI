
package com.jobpilot.email.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.email.entity.GmailConnection;
import com.jobpilot.email.repository.GmailConnectionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GmailAccessTokenService {

    private final UserRepository userRepository;
    private final GmailConnectionRepository gmailConnectionRepository;
    private final TokenEncryptionService tokenEncryptionService;
    private final GoogleTokenRefreshService googleTokenRefreshService;


    public String getValidAccessToken(String userEmail) {

        // 1. Find JobPilot user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalStateException("JobPilot user not found."));

        // 2. Find connected Gmail account
        GmailConnection connection = gmailConnectionRepository.findByUser(user)
                .orElseThrow(() ->
                        new IllegalStateException("Gmail account is not connected."));

        // 3. Check token expiry (60-second safety buffer)
        Instant expiresAt = connection.getAccessTokenExpiresAt();

        if (expiresAt == null ||
                !expiresAt.isAfter(Instant.now().plusSeconds(60))) {

            // 4. Refresh expired or nearly expired token
            return googleTokenRefreshService.refreshAccessToken(connection);
        }

        // 5. Return existing decrypted access token
        return tokenEncryptionService.decrypt(
                connection.getAccessToken()
        );
    }
}
