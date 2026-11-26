
package com.jobpilot.email.dto;

import com.jobpilot.email.entity.EmailStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class EmailResponse {

    private Long id;

    private String recipientEmail;

    private String recipientName;

    private String subject;

    private String body;

    private EmailStatus status;

    private String gmailMessageId;

    private String failureReason;

    private Instant createdAt;

    private Instant sentAt;
}
