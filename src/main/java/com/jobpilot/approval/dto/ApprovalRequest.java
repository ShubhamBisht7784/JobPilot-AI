package com.jobpilot.approval.dto;

import com.jobpilot.approval.entity.ApprovalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRequest {

    @NotNull(message = "Approval type is required")
    private ApprovalType type;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

   

    @Size(max = 10000, message = "Payload cannot exceed 10000 characters")
private String payload;
}