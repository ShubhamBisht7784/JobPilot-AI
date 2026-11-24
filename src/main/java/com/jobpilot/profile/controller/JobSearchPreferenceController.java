package com.jobpilot.profile.controller;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.profile.dto.JobSearchPreferenceRequest;
import com.jobpilot.profile.dto.JobSearchPreferenceResponse;
import com.jobpilot.profile.service.JobSearchPreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/job-preferences")
public class JobSearchPreferenceController {
    private final JobSearchPreferenceService service;
    private final UserRepository userRepository;

    public JobSearchPreferenceController(JobSearchPreferenceService service, UserRepository userRepository) {
        this.service = service; this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<JobSearchPreferenceResponse> get(Authentication authentication) {
        return ResponseEntity.ok(service.get(currentUser(authentication)));
    }

    @PutMapping
    public ResponseEntity<JobSearchPreferenceResponse> save(
            @Valid @RequestBody JobSearchPreferenceRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(service.save(currentUser(authentication), request));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }
}
