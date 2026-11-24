package com.jobpilot.recruiter.dto;

public record RecruiterEmailResult(

        String email,
        String firstName,
        String lastName,
        String company,
        String position,
        String linkedinUrl,
        Integer score,
        String verificationStatus,
        String provenance

) {
}