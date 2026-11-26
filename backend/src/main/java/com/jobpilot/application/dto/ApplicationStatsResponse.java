package com.jobpilot.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationStatsResponse {

    private long totalApplications;

    private long saved;

    private long applied;

    private long interviews;

    private long offers;

    private long rejected;

    private long withdrawn;
}