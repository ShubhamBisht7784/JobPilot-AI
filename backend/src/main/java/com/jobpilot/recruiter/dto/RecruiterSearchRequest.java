package com.jobpilot.recruiter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecruiterSearchRequest(

        @NotBlank(message = "Company name is required")
        @Size(max = 150)
        String companyName,

        @NotBlank(message = "Designation is required")
        @Size(max = 150)
        String designation,

        @Size(max = 100)
        String location

) {
}