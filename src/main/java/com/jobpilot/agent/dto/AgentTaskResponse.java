
package com.jobpilot.agent.dto;

import java.time.Instant;

import com.jobpilot.agent.enums.AgentTaskStatus;

public record AgentTaskResponse(

        Long id,
        String taskName,
        String description,
        AgentTaskStatus status,
        String resultSummary,
        String errorMessage,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt

) {
}
