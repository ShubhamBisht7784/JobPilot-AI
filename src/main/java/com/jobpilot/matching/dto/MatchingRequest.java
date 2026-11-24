package com.jobpilot.matching.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingRequest {

    @NotNull(message = "Resume ID is required")
    @Positive(message = "Resume ID must be positive")
    private Long resumeId;

    @NotNull(message = "Job ID is required")
    @Positive(message = "Job ID must be positive")
    private Long jobId;
}