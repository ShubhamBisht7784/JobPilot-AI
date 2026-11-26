package com.jobpilot.profile.dto;

import java.time.Instant;

public record JobSearchPreferenceResponse(
        String searchKeywords,
        String preferredLocations,
        boolean remoteOnly,
        boolean autoDiscoveryEnabled,
        int minimumMatchScore,
        Instant updatedAt
) {}
