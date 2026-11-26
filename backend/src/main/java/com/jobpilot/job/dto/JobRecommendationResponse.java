package com.jobpilot.job.dto;

import com.jobpilot.job.entity.JobSource;
import java.time.Instant;
import java.util.List;

public record JobRecommendationResponse(
        Long jobId,
        String title,
        String companyName,
        String location,
        String sourceName,
        String salary,
        String employmentType,
        String workMode,
        String description,
        String applyUrl,
        JobSource source,
        Instant postedAt,
        Instant discoveredAt,
        double matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String recommendation,
        String matchingMethod
) {}
