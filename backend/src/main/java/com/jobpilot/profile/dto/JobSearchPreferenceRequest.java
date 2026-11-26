package com.jobpilot.profile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobSearchPreferenceRequest(
        @NotBlank @Size(max = 2000) String searchKeywords,
        @Size(max = 1000) String preferredLocations,
        boolean remoteOnly,
        boolean autoDiscoveryEnabled,
        @Min(0) @Max(100) int minimumMatchScore
) {}
