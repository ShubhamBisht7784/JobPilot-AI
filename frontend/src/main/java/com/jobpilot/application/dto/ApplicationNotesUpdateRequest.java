package com.jobpilot.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationNotesUpdateRequest {

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;
}