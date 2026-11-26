
package com.jobpilot.config;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.email.entity.GmailConnection;
import com.jobpilot.email.service.GmailConnectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequiredArgsConstructor
public class OAuth2TestController {

    private final UserRepository userRepository;
    private final GmailConnectionService gmailConnectionService;

    @GetMapping("/oauth2/success")
    public Map<String, Object> success(
            @AuthenticationPrincipal OAuth2User googleUser,
            @RegisteredOAuth2AuthorizedClient("google")
            OAuth2AuthorizedClient authorizedClient) {

        String gmailAddress = googleUser.getAttribute("email");

        Boolean emailVerified = googleUser.getAttribute("email_verified");

        if (gmailAddress == null || !Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalStateException(
                    "Google email is missing or not verified."
            );
        }

        User user = userRepository.findByEmail(gmailAddress)
                .orElseThrow(() -> new IllegalStateException(
                        "No JobPilot account found for this Google email."
                ));

        var accessToken = authorizedClient.getAccessToken();
        var refreshToken = authorizedClient.getRefreshToken();

        GmailConnection connection = gmailConnectionService.saveConnection(
                user,
                gmailAddress,
                accessToken.getTokenValue(),
                refreshToken != null ? refreshToken.getTokenValue() : null,
                accessToken.getExpiresAt(),
                accessToken.getScopes()
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Gmail connected successfully");
        response.put("gmailAddress", connection.getGmailAddress());
        response.put("connectedAt", connection.getConnectedAt());
        response.put("tokenExpiresAt", connection.getAccessTokenExpiresAt());
        response.put("refreshTokenReceived", refreshToken != null);
        return response;
    }
}
