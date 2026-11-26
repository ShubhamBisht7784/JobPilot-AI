package com.jobpilot.matching.controller;

import com.jobpilot.matching.dto.MatchHistoryResponse;
import com.jobpilot.matching.dto.MatchingRequest;
import com.jobpilot.matching.dto.MatchingResponse;
import com.jobpilot.matching.service.MatchingService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<MatchingResponse> analyzeResume(
            @Valid @RequestBody MatchingRequest request,
            Authentication authentication) {

        MatchingResponse response = matchingService.matchResumeToJob(
                request.getResumeId(),
                request.getJobId(),
                authentication
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<MatchHistoryResponse>> getMatchingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters"
            );
        }

        Page<MatchHistoryResponse> history
                = matchingService.getMatchingHistory(
                        authentication, page, size
                );

        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<MatchHistoryResponse> getMatchingHistoryById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        MatchHistoryResponse response
                = matchingService.getMatchingHistoryById(id, authentication);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<String> deleteMatchingHistory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        matchingService.deleteMatchingHistory(id, authentication);

        return ResponseEntity.ok("Matching history deleted successfully");
    }
}
