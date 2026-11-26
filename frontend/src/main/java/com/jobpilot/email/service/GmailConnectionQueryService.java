
package com.jobpilot.email.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.email.dto.GmailConnectionResponse;
import com.jobpilot.email.entity.GmailConnection;
import com.jobpilot.email.repository.GmailConnectionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GmailConnectionQueryService {

    private final UserRepository userRepository;
    private final GmailConnectionRepository gmailConnectionRepository;

    @Transactional(readOnly = true)
    public GmailConnectionResponse getConnectionStatus(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalStateException("JobPilot user not found."));

        GmailConnection connection = gmailConnectionRepository
                .findByUser(user)
                .orElse(null);

        if (connection == null) {
            return new GmailConnectionResponse(
                    false,
                    null,
                    null,
                    null,
                    null
            );
        }

        return new GmailConnectionResponse(
                true,
                connection.getGmailAddress(),
                connection.getConnectedAt(),
                connection.getAccessTokenExpiresAt(),
                connection.getScopes()
        );
    }
}
