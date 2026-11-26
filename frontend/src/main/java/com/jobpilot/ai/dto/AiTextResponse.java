package com.jobpilot.ai.dto;

import java.time.Instant;

public record AiTextResponse(
        String operation,
        Long resumeId,
        Long jobId,
        String content,
        Instant generatedAt
) {
}
