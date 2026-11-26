
package com.jobpilot.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentTaskRequest(

        @NotBlank(message = "Task name is required")
        @Size(max = 150, message = "Task name cannot exceed 150 characters")
        String taskName,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description

) {
}
