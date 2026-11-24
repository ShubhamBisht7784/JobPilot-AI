
package com.jobpilot.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid recipient email")
    @Size(max = 255)
    private String recipientEmail;

    @Size(max = 255)
    private String recipientName;

    @NotBlank(message = "Email subject is required")
    @Size(max = 500)
    private String subject;

    @NotBlank(message = "Email body is required")
    @Size(max = 20000)
    private String body;
}
