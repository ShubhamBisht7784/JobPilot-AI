
package com.jobpilot.job.dto;

import java.time.Instant;

import com.jobpilot.job.entity.JobSource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String location;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Job URL is required")
    private String jobUrl;

    @NotNull(message = "Job source is required")
    private JobSource source;

    private Instant postedAt;
}
