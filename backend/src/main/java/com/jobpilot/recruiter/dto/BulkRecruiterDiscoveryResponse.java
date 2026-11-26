package com.jobpilot.recruiter.dto;

import java.util.List;

public record BulkRecruiterDiscoveryResponse(
        int totalProfilesFound,
        int profilesProcessed,
        int emailsFound,
        int recruitersReturned,
        int failedProfiles,
        List<RecruiterResponse> recruiters,
        List<RecruiterDiscoveryFailure> failures
) {
}