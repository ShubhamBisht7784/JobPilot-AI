package com.jobpilot.job.controller;

import com.jobpilot.job.dto.JobDiscoveryRequest;
import com.jobpilot.job.dto.JobDiscoveryResponse;
import com.jobpilot.job.service.JobDiscoveryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobDiscoveryController {

    private final JobDiscoveryService jobDiscoveryService;

    @PostMapping("/discover")
    public ResponseEntity<JobDiscoveryResponse> discoverJobs(
            @Valid @RequestBody JobDiscoveryRequest request) {
        return ResponseEntity.ok(jobDiscoveryService.discover(request.query().trim()));
    }
}
