package com.jobpilot.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiQuestionRequest(
        @NotBlank(message = "Question is required")
        @Size(max = 4000, message = "Question cannot exceed 4000 characters")
        String question
) {
}
