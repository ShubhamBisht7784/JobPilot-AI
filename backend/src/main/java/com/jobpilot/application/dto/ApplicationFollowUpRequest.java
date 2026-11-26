package com.jobpilot.application.dto;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationFollowUpRequest {

    @Future(message = "Follow-up date must be in the future")
    private Instant nextFollowUpAt;
}