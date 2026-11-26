package com.jobpilot.ai.controller;

import com.jobpilot.ai.dto.AiIndexResponse;
import com.jobpilot.ai.dto.AiQuestionRequest;
import com.jobpilot.ai.dto.AiTextResponse;
import com.jobpilot.ai.dto.ApplicationPreparationRequest;
import com.jobpilot.ai.dto.ApplicationPreparationResponse;
import com.jobpilot.ai.service.AiResumeService;
import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiResumeService aiResumeService;
    private final UserRepository userRepository;

    @PostMapping("/resumes/{resumeId}/analyze")
    public ResponseEntity<AiTextResponse> analyzeResume(
            @PathVariable Long resumeId,
            @RequestParam(required = false) Long jobId,
            Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(aiResumeService.analyzeResume(resumeId, jobId, user));
    }

    @PostMapping("/resumes/{resumeId}/tailor")
    public ResponseEntity<AiTextResponse> tailorResume(
            @PathVariable Long resumeId,
            @RequestParam Long jobId,
            Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(aiResumeService.tailorResume(resumeId, jobId, user));
    }

    @PostMapping("/resumes/{resumeId}/index")
    public ResponseEntity<AiIndexResponse> indexResume(
            @PathVariable Long resumeId,
            Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(aiResumeService.indexResume(resumeId, user));
    }

    @PostMapping("/resumes/{resumeId}/chat")
    public ResponseEntity<AiTextResponse> askResume(
            @PathVariable Long resumeId,
            @Valid @RequestBody AiQuestionRequest request,
            Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(
                aiResumeService.askResume(resumeId, request.question(), user));
    }

    @PostMapping("/applications/{applicationId}/prepare")
    public ResponseEntity<ApplicationPreparationResponse> prepareApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationPreparationRequest request,
            Authentication authentication) {
        User user = getUser(authentication);
        return ResponseEntity.ok(
                aiResumeService.prepareApplication(applicationId, request, user));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }
}
