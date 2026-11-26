package com.jobpilot.agent.dto;

import jakarta.validation.constraints.NotNull;

public record AgentResumeAnalysisRequest(
        @NotNull(message = "Resume ID is required")
        Long resumeId,
        Long jobId
) {
}
