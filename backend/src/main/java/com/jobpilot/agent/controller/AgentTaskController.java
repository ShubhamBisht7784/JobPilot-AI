package com.jobpilot.agent.controller;

import com.jobpilot.agent.dto.AgentMatchingRequest;
import com.jobpilot.agent.dto.AgentJobDiscoveryRequest;
import com.jobpilot.agent.dto.AgentResumeAnalysisRequest;
import com.jobpilot.ai.dto.AiTextResponse;
import com.jobpilot.agent.dto.AgentTaskRequest;
import com.jobpilot.agent.dto.AgentTaskResponse;
import com.jobpilot.agent.enums.AgentTaskStatus;
import com.jobpilot.agent.service.AgentTaskService;
import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.matching.dto.MatchingResponse;
import com.jobpilot.job.dto.JobDiscoveryResponse;
import com.jobpilot.recruiter.dto.BulkRecruiterDiscoveryResponse;
import com.jobpilot.recruiter.dto.RecruiterBulkDiscoveryRequest;

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
@RequestMapping("/api/agent/tasks")
@RequiredArgsConstructor
public class AgentTaskController {

    private final AgentTaskService agentTaskService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<AgentTaskResponse> createTask(
            @Valid @RequestBody AgentTaskRequest request,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        AgentTaskResponse response = agentTaskService.createTask(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<AgentTaskResponse>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AgentTaskStatus status,
            Authentication authentication) {
        validatePagination(page, size);
        User user = getAuthenticatedUser(authentication);
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AgentTaskResponse> response = status == null
                ? agentTaskService.getUserTasks(user, pageable)
                : agentTaskService.getTasksByStatus(user, status, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentTaskResponse> getTaskById(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(agentTaskService.getTaskById(id, user));
    }

    @PostMapping("/{id}/execute/job-discovery")
    public ResponseEntity<JobDiscoveryResponse> executeJobDiscoveryTask(
            @PathVariable Long id,
            @Valid @RequestBody AgentJobDiscoveryRequest request,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(
                agentTaskService.executeJobDiscoveryTask(id, request, user));
    }

    @PostMapping("/{id}/execute/resume-analysis")
    public ResponseEntity<AiTextResponse> executeResumeAnalysisTask(
            @PathVariable Long id,
            @Valid @RequestBody AgentResumeAnalysisRequest request,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(
                agentTaskService.executeResumeAnalysisTask(id, request, user));
    }

    @PostMapping("/{id}/execute/matching")
    public ResponseEntity<MatchingResponse> executeMatchingTask(
            @PathVariable Long id,
            @Valid @RequestBody AgentMatchingRequest request,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(agentTaskService.executeMatchingTask(id, request, user));
    }

    @PostMapping("/{id}/execute/recruiter-discovery")
    public ResponseEntity<BulkRecruiterDiscoveryResponse> executeRecruiterDiscoveryTask(
            @PathVariable Long id,
            @Valid @RequestBody RecruiterBulkDiscoveryRequest request,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(
                agentTaskService.executeRecruiterDiscoveryTask(id, request, user));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTask(
            @PathVariable Long id,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        agentTaskService.cancelTask(id, user);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated user not found."));
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 50.");
        }
    }
}
