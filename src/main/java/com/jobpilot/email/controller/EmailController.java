package com.jobpilot.email.controller;

import com.jobpilot.email.dto.EmailRequest;
import com.jobpilot.email.dto.EmailResponse;
import com.jobpilot.email.entity.EmailStatus;
import com.jobpilot.email.service.EmailService;
import com.jobpilot.approval.dto.ApprovalResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<EmailResponse> createEmail(
            @Valid @RequestBody EmailRequest request,
            Authentication authentication) {

        EmailResponse response
                = emailService.createEmail(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EmailResponse>> getEmails(
            @RequestParam(required = false) EmailStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<EmailResponse> emails;

        if (status != null) {
            emails = emailService.getEmailsByStatus(
                    status, authentication, pageable);
        } else {
            emails = emailService.getUserEmails(
                    authentication, pageable);
        }

        return ResponseEntity.ok(emails);
    }

    @PostMapping("/{id}/request-approval")
    public ResponseEntity<ApprovalResponse> requestEmailApproval(
            @PathVariable Long id,
            Authentication authentication) {

        ApprovalResponse response
                = emailService.requestEmailApproval(id, authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailResponse> getEmailById(
            @PathVariable Long id,
            Authentication authentication) {

        EmailResponse response
                = emailService.getEmailById(id, authentication);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
public ResponseEntity<EmailResponse> updateEmail(
        @PathVariable Long id,
        @Valid @RequestBody EmailRequest request,
        Authentication authentication) {

    EmailResponse response =
            emailService.updateEmail(id, request, authentication);

    return ResponseEntity.ok(response);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteEmail(
        @PathVariable Long id,
        Authentication authentication) {

    emailService.deleteEmail(id, authentication);

    return ResponseEntity.noContent().build();
}
}
