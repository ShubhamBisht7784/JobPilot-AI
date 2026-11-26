
package com.jobpilot.recruiter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RecruiterRequest(

        @NotBlank(message = "Recruiter name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Recruiter email is required")
        @Email(message = "Please provide a valid email")
        @Size(max = 255)
        String email,

        @Size(max = 150)
        String designation,

        @NotBlank(message = "Company name is required")
        @Size(max = 150)
        String companyName,

        @Pattern(
                regexp = "^https?://(www\\.)?linkedin\\.com/in/[^\\s]+/?$",
                message = "Enter a valid LinkedIn profile URL"
        )
        @Size(max = 500)
        String linkedinUrl,

        @Size(max = 5000)
        String notes

) {
}
