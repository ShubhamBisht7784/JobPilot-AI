package com.jobpilot.recruiter.dto;

public record RecruiterSearchResult(

        String name,
        String designation,
        String companyName,
        String profileUrl,
        String snippet,
        String source

) {
}