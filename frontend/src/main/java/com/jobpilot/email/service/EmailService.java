package com.jobpilot.email.service;

import com.jobpilot.approval.dto.ApprovalRequest;
import com.jobpilot.approval.dto.ApprovalResponse;
import com.jobpilot.approval.entity.ApprovalType;
import com.jobpilot.approval.service.ApprovalService;
import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.email.dto.EmailRequest;
import com.jobpilot.email.dto.EmailResponse;
import com.jobpilot.email.entity.Email;
import com.jobpilot.email.entity.EmailStatus;
import com.jobpilot.email.repository.EmailRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;
    private final UserRepository userRepository;
    private final ApprovalService approvalService;

    @Transactional
    public EmailResponse createEmail(
            EmailRequest request,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        Email email = new Email();

        email.setUser(user);
        email.setRecipientEmail(request.getRecipientEmail());
        email.setRecipientName(request.getRecipientName());
        email.setSubject(request.getSubject());
        email.setBody(request.getBody());

        email.setStatus(EmailStatus.DRAFT);

        Email savedEmail = emailRepository.save(email);

        return convertToResponse(savedEmail);
    }

    private EmailResponse convertToResponse(Email email) {

        return new EmailResponse(
                email.getId(),
                email.getRecipientEmail(),
                email.getRecipientName(),
                email.getSubject(),
                email.getBody(),
                email.getStatus(),
                email.getGmailMessageId(),
                email.getFailureReason(),
                email.getCreatedAt(),
                email.getSentAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<EmailResponse> getUserEmails(
            Authentication authentication,
            Pageable pageable) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        return emailRepository
                .findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<EmailResponse> getEmailsByStatus(
            EmailStatus status,
            Authentication authentication,
            Pageable pageable) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        return emailRepository
                .findByUserAndStatusOrderByCreatedAtDesc(
                        user, status, pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    public ApprovalResponse requestEmailApproval(
            Long emailId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));

        Email email = emailRepository.findByIdAndUser(emailId, user)
                .orElseThrow(()
                        -> new IllegalArgumentException("Email not found"));

        if (email.getStatus() != EmailStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "Only draft emails can be submitted for approval");
        }

        ApprovalRequest approvalRequest = new ApprovalRequest();

        approvalRequest.setType(ApprovalType.EMAIL_SEND);

        approvalRequest.setDescription(
                "Approval required to send email to "
                + email.getRecipientEmail());

        approvalRequest.setPayload(
                "{\"emailId\":" + email.getId() + "}");

        ApprovalResponse approvalResponse
                = approvalService.createApproval(
                        approvalRequest, authentication);

        email.setApprovalId(approvalResponse.getId());
        email.setStatus(EmailStatus.PENDING_APPROVAL);

        emailRepository.save(email);

        return approvalResponse;
    }

    @Transactional(readOnly = true)
public EmailResponse getEmailById(
        Long emailId,
        Authentication authentication) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    Email email = emailRepository.findByIdAndUser(emailId, user)
            .orElseThrow(() ->
                    new IllegalArgumentException("Email not found"));

    return convertToResponse(email);
}

@Transactional
public EmailResponse updateEmail(
        Long emailId,
        EmailRequest request,
        Authentication authentication) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    Email email = emailRepository.findByIdAndUser(emailId, user)
            .orElseThrow(() ->
                    new IllegalArgumentException("Email not found"));

    if (email.getStatus() != EmailStatus.DRAFT) {
        throw new IllegalArgumentException(
                "Only draft emails can be updated");
    }

    email.setRecipientEmail(request.getRecipientEmail());
    email.setRecipientName(request.getRecipientName());
    email.setSubject(request.getSubject());
    email.setBody(request.getBody());

    Email updatedEmail = emailRepository.save(email);

    return convertToResponse(updatedEmail);
}

@Transactional
public void deleteEmail(
        Long emailId,
        Authentication authentication) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    Email email = emailRepository.findByIdAndUser(emailId, user)
            .orElseThrow(() ->
                    new IllegalArgumentException("Email not found"));

    if (email.getStatus() != EmailStatus.DRAFT) {
        throw new IllegalArgumentException(
                "Only draft emails can be deleted");
    }

    emailRepository.delete(email);
}
}
