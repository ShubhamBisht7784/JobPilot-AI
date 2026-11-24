package com.jobpilot.email.service;

import com.jobpilot.approval.event.ApprovalRetryEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class ApprovalRetryListener {

    private final GmailEmailSendingService gmailEmailSendingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalRetry(ApprovalRetryEvent event) {
        gmailEmailSendingService.retryApprovedEmail(event.approvalId());
    }
}
