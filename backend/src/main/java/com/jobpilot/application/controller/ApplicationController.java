package com.jobpilot.application.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobpilot.application.dto.ApplicationFollowUpRequest;
import com.jobpilot.application.dto.ApplicationNotesUpdateRequest;
import com.jobpilot.application.dto.ApplicationRequest;
import com.jobpilot.application.dto.ApplicationResponse;
import com.jobpilot.application.dto.ApplicationStatsResponse;
import com.jobpilot.application.dto.ApplicationStatusUpdateRequest;
import com.jobpilot.application.entity.ApplicationStatus;
import com.jobpilot.application.service.ApplicationService;
import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication) {

        ApplicationResponse response
                = applicationService.createApplication(request, authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getUserApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ApplicationStatus status,
            Authentication authentication) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50");
        }

        Page<ApplicationResponse> applications
                = applicationService.getUserApplications(
                        authentication, page, size, status);

        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            Authentication authentication) {

        ApplicationResponse response
                = applicationService.updateApplicationStatus(
                        id,
                        request.getStatus(),
                        authentication
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ApplicationResponse> updateApplicationNotes(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationNotesUpdateRequest request,
            Authentication authentication) {

        ApplicationResponse response
                = applicationService.updateApplicationNotes(
                        id,
                        request.getNotes(),
                        authentication
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteApplication(
            @PathVariable Long id,
            Authentication authentication) {

        applicationService.deleteApplication(id, authentication);

        return ResponseEntity.ok("Application deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @PathVariable Long id,
            Authentication authentication) {

        ApplicationResponse response
                = applicationService.getApplicationById(id, authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<ApplicationStatsResponse> getApplicationStats(
            Authentication authentication) {

        ApplicationStatsResponse response
                = applicationService.getApplicationStats(authentication);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/follow-up")
    public ResponseEntity<ApplicationResponse> updateApplicationFollowUp(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationFollowUpRequest request,
            Authentication authentication) {

        ApplicationResponse response = applicationService.updateApplicationFollowUp(
                id,
                request,
                authentication
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/follow-ups/due")
    public ResponseEntity<List<ApplicationResponse>> getDueFollowUps(
            Authentication authentication) {

        List<ApplicationResponse> response
                = applicationService.getDueFollowUps(authentication);

        return ResponseEntity.ok(response);
    }

        @GetMapping("/follow-ups/upcoming")
    public ResponseEntity<List<ApplicationResponse>> getUpcomingFollowUps(
            Authentication authentication) {

        List<ApplicationResponse> response =
                applicationService.getUpcomingFollowUps(authentication);

        return ResponseEntity.ok(response);
}
}
