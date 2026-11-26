package com.jobpilot.approval.dto;

import com.jobpilot.approval.entity.ApprovalStatus;
import com.jobpilot.approval.entity.ApprovalType;
import com.jobpilot.approval.entity.ApprovalExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ApprovalResponse {

    private Long id;

    private ApprovalType type;

    private ApprovalStatus status;

    private String description;

    private Instant createdAt;

    private Instant decidedAt;

    private ApprovalExecutionStatus executionStatus;

    private String executionError;

    private Integer executionAttempts;

    private Instant executionStartedAt;
private Instant executionCompletedAt;
}