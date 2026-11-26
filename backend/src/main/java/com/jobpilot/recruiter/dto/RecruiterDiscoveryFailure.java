package com.jobpilot.recruiter.dto;

public record RecruiterDiscoveryFailure(
        String linkedinUrl,
        String reason,
        Integer providerStatus
) {
}