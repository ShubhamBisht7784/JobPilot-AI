
package com.jobpilot.email.controller;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.email.dto.GmailConnectionResponse;
import com.jobpilot.email.service.GmailConnectionQueryService;
import com.jobpilot.email.service.GmailConnectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
public class GmailConnectionController {

    private final GmailConnectionQueryService gmailConnectionQueryService;
    private final GmailConnectionService gmailConnectionService;
    private final UserRepository userRepository;

    @GetMapping("/connection")
    public GmailConnectionResponse getConnectionStatus(
            Authentication authentication) {

        return gmailConnectionQueryService.getConnectionStatus(
                authentication.getName()
        );
    }

    @DeleteMapping("/connection")
    public String disconnectGmail(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("JobPilot user not found."));

        gmailConnectionService.disconnectGmail(user);

        return "Gmail account disconnected successfully.";
    }
}
