package com.jobpilot.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class MatchHistoryResponse {

    private Long id;

    private Long resumeId;

    private String resumeFileName;

    private Long jobId;

    private String jobTitle;

    private String companyName;

    private double matchScore;

    private List<String> matchedSkills;

    private List<String> missingSkills;

    private String recommendation;

    private Instant createdAt;
}