package com.jobpilot.application.dto;

import java.time.Instant;

import com.jobpilot.application.entity.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;

    private Long jobId;

    private String jobTitle;

    private String companyName;

    private String location;

    private String jobUrl;

    private ApplicationStatus status;

    private Instant appliedAt;

    private Instant lastUpdatedAt;

    private String notes;

    private Instant nextFollowUpAt;
}