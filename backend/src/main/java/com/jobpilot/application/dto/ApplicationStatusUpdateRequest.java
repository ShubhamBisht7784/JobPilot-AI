package com.jobpilot.application.dto;

import com.jobpilot.application.entity.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Application status is required")
    private ApplicationStatus status;
}