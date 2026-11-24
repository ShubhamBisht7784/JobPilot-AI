package com.jobpilot.approval.service;

import java.time.Instant;

import com.jobpilot.approval.dto.ApprovalRequest;
import com.jobpilot.approval.dto.ApprovalResponse;
import com.jobpilot.approval.entity.Approval;
import com.jobpilot.approval.entity.ApprovalExecutionStatus;
import com.jobpilot.approval.entity.ApprovalStatus;
import com.jobpilot.approval.entity.ApprovalType;
import com.jobpilot.approval.event.ApprovalDecisionEvent;
import com.jobpilot.approval.repository.ApprovalRepository;
import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.common.exception.ApprovalNotFoundException;
import com.jobpilot.approval.event.ApprovalRetryEvent;
import com.jobpilot.email.entity.EmailStatus;
import com.jobpilot.email.repository.EmailRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailRepository emailRepository;

    private static final int MAX_EXECUTION_ATTEMPTS = 3;
    private static final int MAX_ERROR_LENGTH = 1000;

    // Create Approval
    @Transactional
    public ApprovalResponse createApproval(
            ApprovalRequest request,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        if (request.getType() != ApprovalType.EMAIL_SEND) {
            throw new IllegalArgumentException(
                    "Only EMAIL_SEND approvals are executable in this backend. "
                    + "Job applications must be reviewed and submitted by the user on the employer's website.");
        }

        Approval approval = new Approval();

        approval.setUser(user);
        approval.setType(request.getType());
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setDescription(request.getDescription());
        approval.setPayload(request.getPayload());

        Approval savedApproval = approvalRepository.save(approval);

        return convertToResponse(savedApproval);
    }

    // Convert Entity to Response DTO
    private ApprovalResponse convertToResponse(Approval approval) {

        return new ApprovalResponse(
                approval.getId(),
                approval.getType(),
                approval.getStatus(),
                approval.getDescription(),
                approval.getCreatedAt(),
                approval.getDecidedAt(),
                approval.getExecutionStatus(),
                approval.getExecutionError(),
                approval.getExecutionAttempts(),
                approval.getExecutionStartedAt(),
                approval.getExecutionCompletedAt()
        );
    }

    // Get User Approvals with Pagination
    @Transactional(readOnly = true)
    public Page<ApprovalResponse> getUserApprovals(
            Authentication authentication,
            Pageable pageable) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        return approvalRepository
                .findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToResponse);
    }

    // Get Approvals by Status with Pagination
    @Transactional(readOnly = true)
    public Page<ApprovalResponse> getApprovalsByStatus(
            ApprovalStatus status,
            Authentication authentication,
            Pageable pageable) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        return approvalRepository
                .findByUserAndStatusOrderByCreatedAtDesc(
                        user, status, pageable)
                .map(this::convertToResponse);
    }

    // Approve or Reject
    @Transactional
    public ApprovalResponse decideApproval(
            Long approvalId,
            ApprovalStatus decision,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        Approval approval = approvalRepository
                .findByIdAndUser(approvalId, user)
                .orElseThrow(()
                        -> new ApprovalNotFoundException("Approval not found"));

        if (decision == null) {
            throw new IllegalArgumentException(
                    "Approval decision is required");
        }

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending approvals can be decided");
        }

        if (decision != ApprovalStatus.APPROVED
                && decision != ApprovalStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Invalid approval decision");
        }

        approval.setStatus(decision);
        approval.setDecidedAt(Instant.now());

        Approval updatedApproval = approvalRepository.save(approval);

// Publish event after approval decision
        eventPublisher.publishEvent(
                new ApprovalDecisionEvent(
                        updatedApproval.getId(),
                        updatedApproval.getStatus()
                )
        );

        return convertToResponse(updatedApproval);
    }

    // Get Approval by ID
    @Transactional(readOnly = true)
    public ApprovalResponse getApprovalById(
            Long approvalId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        Approval approval = approvalRepository
                .findByIdAndUser(approvalId, user)
                .orElseThrow(()
                        -> new ApprovalNotFoundException("Approval not found"));

        return convertToResponse(approval);
    }

    // Mark Execution In Progress
    @Transactional
    public void markExecutionInProgress(Long approvalId) {

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(()
                        -> new ApprovalNotFoundException("Approval not found"));

        if (approval.getStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Cannot execute an unapproved action");
        }

        if (approval.getExecutionStatus()
                != ApprovalExecutionStatus.NOT_STARTED) {
            throw new IllegalArgumentException(
                    "Action execution has already started");
        }

        approval.setExecutionStartedAt(Instant.now());

        approval.setExecutionAttempts(
                approval.getExecutionAttempts() + 1);

        approval.setExecutionStatus(
                ApprovalExecutionStatus.IN_PROGRESS);

        approvalRepository.save(approval);
    }

    // Mark Execution Completed
    @Transactional
    public void markExecutionCompleted(Long approvalId) {

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(()
                        -> new ApprovalNotFoundException("Approval not found"));

        if (approval.getExecutionStatus()
                != ApprovalExecutionStatus.IN_PROGRESS) {
            throw new IllegalArgumentException(
                    "Action is not currently in progress");
        }

        approval.setExecutionCompletedAt(Instant.now());

        approval.setExecutionStatus(
                ApprovalExecutionStatus.COMPLETED);

        approvalRepository.save(approval);
    }

    // Mark Execution Failed
    @Transactional
    public void markExecutionFailed(
            Long approvalId,
            String errorMessage) {

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(()
                        -> new ApprovalNotFoundException("Approval not found"));

        if (approval.getExecutionStatus()
                != ApprovalExecutionStatus.IN_PROGRESS) {
            throw new IllegalArgumentException(
                    "Action is not currently in progress");
        }

        approval.setExecutionCompletedAt(Instant.now());

        approval.setExecutionStatus(
                ApprovalExecutionStatus.FAILED);

        String safeError = (errorMessage == null || errorMessage.isBlank())
                ? "Execution failed"
                : errorMessage;

        if (safeError.length() > MAX_ERROR_LENGTH) {
            safeError = safeError.substring(0, MAX_ERROR_LENGTH);
        }

        approval.setExecutionError(safeError);

        approvalRepository.save(approval);
    }

    // Retry Failed Execution
    @Transactional
    public ApprovalResponse retryExecution(Long approvalId, User user) {

        Approval approval = approvalRepository.findByIdAndUser(approvalId, user)
                .orElseThrow(() -> new ApprovalNotFoundException(
                        "Approval not found with id: " + approvalId
                ));

        if (approval.getType() != ApprovalType.EMAIL_SEND) {
            throw new IllegalStateException(
                    "Retry is currently supported only for email approvals."
            );
        }
        var email = emailRepository.findByApprovalId(approvalId)
                .orElseThrow(() -> new IllegalStateException(
                        "Email record for this approval was not found."));
        if (email.getStatus() != EmailStatus.FAILED) {
            throw new IllegalStateException(
                    "Only definite pre-send failures can be retried. If delivery is unknown, check Gmail Sent first.");
        }

        if (approval.getStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException(
                    "Only approved requests can be retried."
            );
        }

        if (approval.getExecutionStatus() != ApprovalExecutionStatus.FAILED) {
            throw new IllegalStateException(
                    "Only failed executions can be retried."
            );
        }

        if (approval.getExecutionAttempts() >= 3) {
            throw new IllegalStateException(
                    "Maximum execution retry attempts reached."
            );
        }

        approval.setExecutionStatus(ApprovalExecutionStatus.NOT_STARTED);
        approval.setExecutionError(null);
        approval.setExecutionCompletedAt(null);

        Approval updatedApproval = approvalRepository.save(approval);

        eventPublisher.publishEvent(
                new ApprovalRetryEvent(updatedApproval.getId())
        );

        return convertToResponse(updatedApproval);
    }
}
