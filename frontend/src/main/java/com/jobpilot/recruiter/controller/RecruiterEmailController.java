package com.jobpilot.recruiter.controller;

import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.recruiter.dto.RecruiterEmailResult;
import com.jobpilot.recruiter.dto.RecruiterEmailSearchRequest;
import com.jobpilot.recruiter.dto.RecruiterResponse;
import com.jobpilot.recruiter.integration.HunterApiException;
import com.jobpilot.recruiter.service.RecruiterEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import com.jobpilot.auth.entity.User;


@RestController
@RequestMapping("/api/recruiters")
@RequiredArgsConstructor
public class RecruiterEmailController {

    private final RecruiterEmailService recruiterEmailService;
    private final UserRepository userRepository;

    @PostMapping("/find-email")
    public ResponseEntity<RecruiterEmailResult> findRecruiterEmail(
            @Valid @RequestBody RecruiterEmailSearchRequest request) {

        return recruiterEmailService
                .findRecruiterEmail(request.linkedinUrl())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(HunterApiException.class)
public ResponseEntity<?> handleHunterApiException(
        HunterApiException exception) {

    return ResponseEntity
            .status(org.springframework.http.HttpStatus.BAD_GATEWAY)
            .body(java.util.Map.of(
                    "message", exception.getMessage(),
                    "providerStatus", exception.getStatusCode()
            ));
}

@PostMapping("/find-email/save")
public ResponseEntity<RecruiterResponse> findAndSaveRecruiter(
        @Valid @RequestBody RecruiterEmailSearchRequest request,
        Authentication authentication) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new IllegalStateException("Authenticated user not found."));

    Optional<RecruiterEmailResult> result =
            recruiterEmailService.findRecruiterEmail(request.linkedinUrl());

    if (result.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    RecruiterResponse savedRecruiter =
            recruiterEmailService.saveDiscoveredRecruiter(
                    result.get(),
                    user
            );

    return ResponseEntity.ok(savedRecruiter);
}
}