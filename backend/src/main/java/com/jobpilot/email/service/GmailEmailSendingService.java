package com.jobpilot.email.service;

import java.time.Instant;

import com.jobpilot.approval.service.ApprovalService;
import com.jobpilot.email.entity.Email;
import com.jobpilot.email.entity.EmailStatus;
import com.jobpilot.email.integration.GmailApiClient;
import com.jobpilot.email.integration.GmailMimeMessageBuilder;
import com.jobpilot.email.repository.EmailRepository;
import com.jobpilot.email.repository.GmailConnectionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailEmailSendingService {

    private final EmailRepository emailRepository;
    private final GmailConnectionRepository gmailConnectionRepository;
    private final GmailAccessTokenService gmailAccessTokenService;
    private final GmailMimeMessageBuilder mimeMessageBuilder;
    private final GmailApiClient gmailApiClient;
    private final ApprovalService approvalService;
    private final PlatformTransactionManager transactionManager;

    public void sendApprovedEmail(Long approvalId) {
        SendSnapshot snapshot = claimForInitialSend(approvalId);
        if (snapshot != null) {
            executeSend(snapshot);
        }
    }

    public void retryApprovedEmail(Long approvalId) {
        SendSnapshot snapshot = claimForRetry(approvalId);
        if (snapshot != null) {
            executeSend(snapshot);
        }
    }

    public void rejectEmail(Long approvalId) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            Email email = emailRepository.findByApprovalIdForUpdate(approvalId)
                    .orElse(null);
            if (email != null && email.getStatus() == EmailStatus.PENDING_APPROVAL) {
                email.setStatus(EmailStatus.REJECTED);
                emailRepository.save(email);
            }
        });
    }

    private SendSnapshot claimForInitialSend(Long approvalId) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> {
            Email email = emailRepository.findByApprovalIdForUpdate(approvalId)
                    .orElseThrow(() -> new IllegalStateException("Email not found."));

            if (email.getStatus() != EmailStatus.PENDING_APPROVAL
                    && email.getStatus() != EmailStatus.APPROVED) {
                return null;
            }

            email.setStatus(EmailStatus.APPROVED);
            emailRepository.save(email);
            approvalService.markExecutionInProgress(approvalId);
            email.setStatus(EmailStatus.SENDING);
            emailRepository.save(email);

            return snapshot(email);
        });
    }

    private SendSnapshot claimForRetry(Long approvalId) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> {
            Email email = emailRepository.findByApprovalIdForUpdate(approvalId)
                    .orElseThrow(() -> new IllegalStateException("Email not found."));

            if (email.getStatus() == EmailStatus.DELIVERY_UNKNOWN) {
                throw new IllegalStateException(
                        "Gmail delivery outcome is unknown. Check the Gmail Sent folder before retrying.");
            }
            if (email.getStatus() != EmailStatus.FAILED) {
                return null;
            }

            email.setStatus(EmailStatus.APPROVED);
            email.setFailureReason(null);
            emailRepository.save(email);
            approvalService.markExecutionInProgress(approvalId);
            email.setStatus(EmailStatus.SENDING);
            emailRepository.save(email);

            return snapshot(email);
        });
    }

    private SendSnapshot snapshot(Email email) {
        String userEmail = email.getUser().getEmail();

        return new SendSnapshot(
                email.getId(),
                email.getApprovalId(),
                userEmail,
                email.getRecipientEmail(),
                email.getSubject(),
                email.getBody());
    }

    private void executeSend(SendSnapshot snapshot) {
        boolean sendRequestStarted = false;

        try {
            var connection = gmailConnectionRepository.findByUser_Email(snapshot.userEmail())
                    .orElseThrow(() -> new IllegalStateException("Gmail account is not connected."));
            String accessToken =
                    gmailAccessTokenService.getValidAccessToken(snapshot.userEmail());

            String rawMessage = mimeMessageBuilder.buildRawMessage(
                    connection.getGmailAddress(),
                    snapshot.recipientEmail(),
                    snapshot.subject(),
                    snapshot.body());

            sendRequestStarted = true;
            var response = gmailApiClient.sendMessage(accessToken, rawMessage);

            if (response == null || response.getId() == null || response.getId().isBlank()) {
                throw new IllegalStateException("Gmail returned no message ID.");
            }

            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.executeWithoutResult(status -> {
                Email email = emailRepository.findById(snapshot.emailId())
                        .orElseThrow(() -> new IllegalStateException("Email record disappeared."));
                if (email.getStatus() != EmailStatus.SENDING) {
                    throw new IllegalStateException("Email is no longer in SENDING state.");
                }
                email.setGmailMessageId(response.getId());
                email.setStatus(EmailStatus.SENT);
                email.setSentAt(Instant.now());
                email.setFailureReason(null);
                emailRepository.save(email);
                approvalService.markExecutionCompleted(snapshot.approvalId());
            });

        } catch (Exception exception) {
            final boolean ambiguous = sendRequestStarted;
            final String safeReason = ambiguous
                    ? "Gmail request was attempted but its delivery outcome is unknown. Check Gmail Sent before retrying."
                    : "Email sending failed before the Gmail send request: "
                            + exception.getClass().getSimpleName();

            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.executeWithoutResult(status -> {
                Email email = emailRepository.findById(snapshot.emailId()).orElse(null);
                if (email != null && email.getStatus() == EmailStatus.SENDING) {
                    email.setStatus(ambiguous
                            ? EmailStatus.DELIVERY_UNKNOWN
                            : EmailStatus.FAILED);
                    email.setFailureReason(safeReason);
                    emailRepository.save(email);
                }
                try {
                    approvalService.markExecutionFailed(snapshot.approvalId(), safeReason);
                } catch (RuntimeException stateException) {
                    log.warn("Could not update approval execution state for approval {}",
                            snapshot.approvalId());
                }
            });
        }
    }

    private record SendSnapshot(
            Long emailId,
            Long approvalId,
            String userEmail,
            String recipientEmail,
            String subject,
            String body) {
    }
}
