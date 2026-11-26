package com.jobpilot.ai.dto;

public record AiIndexResponse(
        Long resumeId,
        int chunksIndexed,
        String retrievalType,
        String message
) {
}
