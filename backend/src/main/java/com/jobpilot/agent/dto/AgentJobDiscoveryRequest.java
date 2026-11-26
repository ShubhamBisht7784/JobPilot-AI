package com.jobpilot.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentJobDiscoveryRequest(
        @NotBlank(message = "Search query is required")
        @Size(max = 250, message = "Search query cannot exceed 250 characters")
        String query
) {
}
