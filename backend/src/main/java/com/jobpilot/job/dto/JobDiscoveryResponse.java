package com.jobpilot.job.dto;

public record JobDiscoveryResponse(
        String query,
        int jobsFound,
        int jobsSaved,
        int duplicatesSkipped
) {
}
