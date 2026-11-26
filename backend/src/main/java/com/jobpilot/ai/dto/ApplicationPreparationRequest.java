package com.jobpilot.ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationPreparationRequest(
        @NotNull(message = "Resume ID is required")
        Long resumeId,

        @Size(max = 3000, message = "Additional context cannot exceed 3000 characters")
        String additionalContext
) {
}
