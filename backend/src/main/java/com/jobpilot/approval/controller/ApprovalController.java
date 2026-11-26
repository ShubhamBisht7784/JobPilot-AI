
package com.jobpilot.approval.controller;

import com.jobpilot.approval.dto.ApprovalRequest;
import com.jobpilot.approval.dto.ApprovalResponse;
import com.jobpilot.approval.entity.ApprovalStatus;
import com.jobpilot.approval.service.ApprovalService;
import com.jobpilot.auth.repository.UserRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.jobpilot.auth.entity.User;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final UserRepository userRepository;

    // Create Approval
    @PostMapping
    public ResponseEntity<ApprovalResponse> createApproval(
            @Valid @RequestBody ApprovalRequest request,
            Authentication authentication) {

        ApprovalResponse response =
                approvalService.createApproval(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get All Approvals with Optional Status Filter and Pagination
    @GetMapping
    public ResponseEntity<Page<ApprovalResponse>> getApprovals(
            @RequestParam(required = false) ApprovalStatus status,
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

        Page<ApprovalResponse> approvals;

        if (status != null) {
            approvals = approvalService.getApprovalsByStatus(
                    status,
                    authentication,
                    pageable
            );
        } else {
            approvals = approvalService.getUserApprovals(
                    authentication,
                    pageable
            );
        }

        return ResponseEntity.ok(approvals);
    }

    // Approve or Reject Approval
    @PatchMapping("/{id}/decision")
    public ResponseEntity<ApprovalResponse> decideApproval(
            @PathVariable Long id,
            @RequestParam ApprovalStatus decision,
            Authentication authentication) {

        ApprovalResponse response = approvalService.decideApproval(
                id,
                decision,
                authentication
        );

        return ResponseEntity.ok(response);
    }

    // Get Approval by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApprovalResponse> getApprovalById(
            @PathVariable Long id,
            Authentication authentication) {

        ApprovalResponse response =
                approvalService.getApprovalById(id, authentication);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retry")
public ResponseEntity<ApprovalResponse> retryExecution(
        @PathVariable Long id,
        Authentication authentication) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("User not found."));

    ApprovalResponse response = approvalService.retryExecution(id, user);

    return ResponseEntity.ok(response);
}
}
