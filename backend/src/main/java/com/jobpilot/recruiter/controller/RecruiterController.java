package com.jobpilot.recruiter.controller;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.recruiter.dto.RecruiterRequest;
import com.jobpilot.recruiter.dto.RecruiterResponse;
import com.jobpilot.recruiter.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiters")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<RecruiterResponse> createRecruiter(
            @Valid @RequestBody RecruiterRequest request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        RecruiterResponse response =
                recruiterService.createRecruiter(request, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<RecruiterResponse>> getRecruiters(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters."
            );
        }

        User user = getAuthenticatedUser(authentication);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                recruiterService.getUserRecruiters(user, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruiterResponse> getRecruiterById(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                recruiterService.getRecruiterById(id, user)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecruiterResponse> updateRecruiter(
            @PathVariable Long id,
            @Valid @RequestBody RecruiterRequest request,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                recruiterService.updateRecruiter(id, request, user)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecruiter(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        recruiterService.deleteRecruiter(id, user);

        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found."
                ));
    }
}