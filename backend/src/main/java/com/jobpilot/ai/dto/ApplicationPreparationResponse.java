package com.jobpilot.ai.dto;

import java.time.Instant;

public record ApplicationPreparationResponse(
        Long applicationId,
        Long resumeId,
        Long jobId,
        String jobTitle,
        String companyName,
        String draft,
        String nextAction,
        Instant generatedAt
) {
}
