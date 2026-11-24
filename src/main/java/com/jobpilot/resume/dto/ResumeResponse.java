
package com.jobpilot.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ResumeResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private Instant uploadedAt;
}
