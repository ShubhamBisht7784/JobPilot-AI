package com.jobpilot.agent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgentMatchingRequest(
        @NotNull(message = "Resume ID is required")
        @Positive(message = "Resume ID must be positive")
        Long resumeId,

        @NotNull(message = "Job ID is required")
        @Positive(message = "Job ID must be positive")
        Long jobId
) {
}
