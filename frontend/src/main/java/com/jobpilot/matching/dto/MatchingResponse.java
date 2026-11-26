
package com.jobpilot.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MatchingResponse {

    private Long resumeId;
    private Long jobId;

    private double matchScore;

    private List<String> matchedSkills;
    private List<String> missingSkills;

    private String recommendation;
}
