package com.jobpilot.job.controller;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.job.dto.JobRecommendationResponse;
import com.jobpilot.job.service.JobRecommendationService;
import com.jobpilot.profile.service.JobSearchPreferenceService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs/recommended")
public class JobRecommendationController {
    private final JobRecommendationService recommendationService;
    private final UserRepository userRepository;
    private final JobSearchPreferenceService preferenceService;

    public JobRecommendationController(JobRecommendationService recommendationService,
            UserRepository userRepository, JobSearchPreferenceService preferenceService) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<Page<JobRecommendationResponse>> getRecommendations(
            @RequestParam(required = false) Double minScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
        double effectiveMinScore = minScore == null
                ? preferenceService.get(user).minimumMatchScore() : minScore;
        if (effectiveMinScore < 0 || effectiveMinScore > 100 || page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException("minScore must be 0-100, page >= 0 and size 1-50.");
        }
        return ResponseEntity.ok(recommendationService.getRecommendations(user, effectiveMinScore, page, size));
    }
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshRecommendations(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
        int processed = recommendationService.scoreNewJobsForUser(user);
        return ResponseEntity.ok(Map.of("newMatchesCreated", processed,
                "message", "Recommendation refresh completed."));
    }

}
