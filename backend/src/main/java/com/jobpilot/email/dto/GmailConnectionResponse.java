
package com.jobpilot.email.dto;

import java.time.Instant;

public record GmailConnectionResponse(
        boolean connected,
        String gmailAddress,
        Instant connectedAt,
        Instant tokenExpiresAt,
        String scopes
) {
}
