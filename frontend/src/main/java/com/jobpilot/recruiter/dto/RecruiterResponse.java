package com.jobpilot.recruiter.dto;

import java.time.Instant;

public record RecruiterResponse(
        Long id,
        String name,
        String email,
        String designation,
        String companyName,
        String linkedinUrl,
        String notes,
        Instant createdAt,
        Integer emailVerificationScore,
        String emailVerificationStatus,
        String emailProvenance
        ) {

}
