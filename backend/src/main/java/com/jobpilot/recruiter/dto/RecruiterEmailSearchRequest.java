package com.jobpilot.recruiter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RecruiterEmailSearchRequest(

        @NotBlank(message = "LinkedIn URL is required")
        @Pattern(
                regexp = "^https?://(www\\.)?linkedin\\.com/in/[^\\s]+/?$",
                message = "Enter a valid LinkedIn profile URL"
        )
        String linkedinUrl

) {}