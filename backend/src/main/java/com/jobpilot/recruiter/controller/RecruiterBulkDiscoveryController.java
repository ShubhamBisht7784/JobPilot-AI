package com.jobpilot.recruiter.controller;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.recruiter.dto.BulkRecruiterDiscoveryResponse;
import com.jobpilot.recruiter.dto.RecruiterBulkDiscoveryRequest;
import com.jobpilot.recruiter.dto.RecruiterResponse;
import com.jobpilot.recruiter.service.BulkRecruiterDiscoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiters")
@RequiredArgsConstructor
public class RecruiterBulkDiscoveryController {

    private final BulkRecruiterDiscoveryService bulkRecruiterDiscoveryService;
    private final UserRepository userRepository;

   @PostMapping("/bulk-discover")
public ResponseEntity<BulkRecruiterDiscoveryResponse> discoverRecruiters(
        @Valid @RequestBody RecruiterBulkDiscoveryRequest request,
        Authentication authentication) {

    User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new IllegalStateException("Authenticated user not found."));

    BulkRecruiterDiscoveryResponse response =
            bulkRecruiterDiscoveryService.discoverAndSaveRecruiters(
                    request,
                    user
            );

    return ResponseEntity.ok(response);
}
}