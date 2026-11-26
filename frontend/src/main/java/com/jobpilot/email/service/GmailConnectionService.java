package com.jobpilot.email.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.email.entity.GmailConnection;
import com.jobpilot.email.repository.GmailConnectionRepository;
import com.jobpilot.email.service.TokenEncryptionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jobpilot.email.service.GoogleTokenRevocationService;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GmailConnectionService {

    private final GmailConnectionRepository gmailConnectionRepository;
    private final TokenEncryptionService tokenEncryptionService;
    private final GoogleTokenRevocationService googleTokenRevocationService;

    @Transactional
    public GmailConnection saveConnection(
            User user,
            String gmailAddress,
            String accessToken,
            String refreshToken,
            Instant expiresAt,
            Set<String> scopes) {

        GmailConnection connection = gmailConnectionRepository
                .findByUser(user)
                .orElseGet(GmailConnection::new);

        connection.setUser(user);
        connection.setGmailAddress(gmailAddress);

        connection.setAccessToken(
                tokenEncryptionService.encrypt(accessToken)
        );

        if (refreshToken != null && !refreshToken.isBlank()) {
            connection.setRefreshToken(
                    tokenEncryptionService.encrypt(refreshToken)
            );
        }

        connection.setAccessTokenExpiresAt(expiresAt);

        connection.setScopes(
                scopes == null ? "" : scopes.stream().collect(Collectors.joining(","))
        );

        return gmailConnectionRepository.save(connection);
    }

  @Transactional
public void disconnectGmail(User user) {

    GmailConnection connection = gmailConnectionRepository
            .findByUser(user)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Gmail account is not connected."
                    ));

    // First revoke Google authorization
    googleTokenRevocationService.revokeGoogleAccess(connection);

    // Delete local connection only after successful revocation
    gmailConnectionRepository.delete(connection);
}
}
