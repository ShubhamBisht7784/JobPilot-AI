package com.jobpilot.recruiter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecruiterBulkDiscoveryRequest(

        @NotBlank(message = "Company name is required")
        @Size(max = 150)
        String companyName,

        @NotBlank(message = "Designation is required")
        @Size(max = 150)
        String designation,

        @Size(max = 100)
        String location,

        @Min(value = 1, message = "At least 1 profile is required")
        @Max(value = 5, message = "Maximum 5 profiles allowed per request")
        Integer maxProfiles

) {}