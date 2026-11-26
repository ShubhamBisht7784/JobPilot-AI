package com.jobpilot.email.integration;

import com.jobpilot.approval.entity.ApprovalStatus;
import com.jobpilot.approval.event.ApprovalDecisionEvent;
import com.jobpilot.email.service.GmailEmailSendingService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class ApprovalDecisionListener {

    private final GmailEmailSendingService gmailEmailSendingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApprovalDecision(ApprovalDecisionEvent event) {
        if (event.decision() == ApprovalStatus.APPROVED) {
            gmailEmailSendingService.sendApprovedEmail(event.approvalId());
        } else if (event.decision() == ApprovalStatus.REJECTED) {
            gmailEmailSendingService.rejectEmail(event.approvalId());
        }
    }
}
