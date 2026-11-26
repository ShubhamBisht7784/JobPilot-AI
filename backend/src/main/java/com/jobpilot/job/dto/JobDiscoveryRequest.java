package com.jobpilot.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobDiscoveryRequest(
        @NotBlank(message = "Search query is required")
        @Size(max = 250, message = "Search query cannot exceed 250 characters")
        String query
) {
}
