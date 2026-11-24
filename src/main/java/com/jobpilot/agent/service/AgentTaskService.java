package com.jobpilot.agent.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobpilot.agent.dto.AgentMatchingRequest;
import com.jobpilot.agent.dto.AgentJobDiscoveryRequest;
import com.jobpilot.agent.dto.AgentResumeAnalysisRequest;
import com.jobpilot.ai.dto.AiTextResponse;
import com.jobpilot.ai.service.AiResumeService;
import com.jobpilot.agent.dto.AgentTaskRequest;
import com.jobpilot.agent.dto.AgentTaskResponse;
import com.jobpilot.agent.entity.AgentTask;
import com.jobpilot.agent.enums.AgentTaskStatus;
import com.jobpilot.agent.repository.AgentTaskRepository;
import com.jobpilot.auth.entity.User;
import com.jobpilot.common.exception.AgentTaskNotFoundException;
import com.jobpilot.matching.dto.MatchingResponse;
import com.jobpilot.matching.service.MatchingService;
import com.jobpilot.job.dto.JobDiscoveryResponse;
import com.jobpilot.job.service.JobDiscoveryService;
import com.jobpilot.recruiter.dto.BulkRecruiterDiscoveryResponse;
import com.jobpilot.recruiter.dto.RecruiterBulkDiscoveryRequest;
import com.jobpilot.recruiter.service.BulkRecruiterDiscoveryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgentTaskService {

    private final AgentTaskRepository agentTaskRepository;
    private final MatchingService matchingService;
    private final AiResumeService aiResumeService;
    private final BulkRecruiterDiscoveryService bulkRecruiterDiscoveryService;
    private final JobDiscoveryService jobDiscoveryService;

    @Transactional
    public AgentTaskResponse createTask(AgentTaskRequest request, User user) {
        AgentTask task = new AgentTask();
        task.setUser(user);
        task.setTaskName(request.taskName().trim());
        task.setDescription(request.description());
        task.setStatus(AgentTaskStatus.PENDING);
        return convertToResponse(agentTaskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<AgentTaskResponse> getUserTasks(User user, Pageable pageable) {
        return agentTaskRepository.findByUser(user, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public AgentTaskResponse getTaskById(Long taskId, User user) {
        return convertToResponse(findTask(taskId, user));
    }

    @Transactional(readOnly = true)
    public Page<AgentTaskResponse> getTasksByStatus(
            User user, AgentTaskStatus status, Pageable pageable) {
        return agentTaskRepository.findByUserAndStatus(user, status, pageable)
                .map(this::convertToResponse);
    }

    public JobDiscoveryResponse executeJobDiscoveryTask(
            Long taskId, AgentJobDiscoveryRequest request, User user) {
        markTaskAsRunning(taskId, user);
        try {
            JobDiscoveryResponse result = jobDiscoveryService.discover(request.query().trim());
            markTaskAsCompleted(taskId, user,
                    "Jobs found: " + result.jobsFound()
                    + ", saved: " + result.jobsSaved()
                    + ", duplicates skipped: " + result.duplicatesSkipped());
            return result;
        } catch (RuntimeException exception) {
            markTaskAsFailed(taskId, user, safeErrorMessage(exception));
            throw exception;
        }
    }

    public AiTextResponse executeResumeAnalysisTask(
            Long taskId, AgentResumeAnalysisRequest request, User user) {
        markTaskAsRunning(taskId, user);
        try {
            AiTextResponse result = aiResumeService.analyzeResume(
                    request.resumeId(), request.jobId(), user);
            markTaskAsCompleted(taskId, user,
                    "AI resume analysis generated for resume " + request.resumeId());
            return result;
        } catch (RuntimeException exception) {
            markTaskAsFailed(taskId, user, safeErrorMessage(exception));
            throw exception;
        }
    }

    public MatchingResponse executeMatchingTask(
            Long taskId, AgentMatchingRequest request, User user) {
        markTaskAsRunning(taskId, user);
        try {
            MatchingResponse result = matchingService.matchResumeToJob(
                    request.resumeId(), request.jobId(), user);

            String summary = "Match score: " + result.getMatchScore()
                    + "%. Recommendation: " + result.getRecommendation()
                    + ". Matched skills: " + String.join(", ", result.getMatchedSkills())
                    + ". Missing skills: " + String.join(", ", result.getMissingSkills());

            markTaskAsCompleted(taskId, user, summary);
            return result;
        } catch (RuntimeException exception) {
            markTaskAsFailed(taskId, user, safeErrorMessage(exception));
            throw exception;
        }
    }

    public BulkRecruiterDiscoveryResponse executeRecruiterDiscoveryTask(
            Long taskId, RecruiterBulkDiscoveryRequest request, User user) {
        markTaskAsRunning(taskId, user);
        try {
            BulkRecruiterDiscoveryResponse result =
                    bulkRecruiterDiscoveryService.discoverAndSaveRecruiters(request, user);

            String summary = "Profiles found: " + result.totalProfilesFound()
                    + ", processed: " + result.profilesProcessed()
                    + ", emails found: " + result.emailsFound()
                    + ", recruiters saved: " + result.recruitersReturned()
                    + ", failed profiles: " + result.failedProfiles();

            markTaskAsCompleted(taskId, user, summary);
            return result;
        } catch (RuntimeException exception) {
            markTaskAsFailed(taskId, user, safeErrorMessage(exception));
            throw exception;
        }
    }

    @Transactional
    public void markTaskAsRunning(Long taskId, User user) {
        AgentTask task = findTask(taskId, user);
        if (task.getStatus() != AgentTaskStatus.PENDING) {
            throw new IllegalStateException("Only pending tasks can be started.");
        }
        task.setStatus(AgentTaskStatus.RUNNING);
        task.setStartedAt(Instant.now());
        agentTaskRepository.save(task);
    }

    @Transactional
    public void markTaskAsCompleted(Long taskId, User user, String resultSummary) {
        AgentTask task = findTask(taskId, user);
        if (task.getStatus() != AgentTaskStatus.RUNNING) {
            throw new IllegalStateException("Only running tasks can be completed.");
        }
        task.setStatus(AgentTaskStatus.COMPLETED);
        task.setResultSummary(resultSummary);
        task.setCompletedAt(Instant.now());
        agentTaskRepository.save(task);
    }

    @Transactional
    public void markTaskAsFailed(Long taskId, User user, String errorMessage) {
        AgentTask task = findTask(taskId, user);
        if (task.getStatus() != AgentTaskStatus.RUNNING) {
            throw new IllegalStateException("Only running tasks can be marked as failed.");
        }
        task.setStatus(AgentTaskStatus.FAILED);
        task.setErrorMessage(safeErrorMessage(errorMessage));
        task.setCompletedAt(Instant.now());
        agentTaskRepository.save(task);
    }

    @Transactional
    public void markTaskAsWaitingForApproval(Long taskId, User user) {
        AgentTask task = findTask(taskId, user);
        if (task.getStatus() != AgentTaskStatus.RUNNING) {
            throw new IllegalStateException("Only running tasks can wait for approval.");
        }
        task.setStatus(AgentTaskStatus.WAITING_FOR_APPROVAL);
        agentTaskRepository.save(task);
    }

    @Transactional
    public void resumeTaskAfterApproval(Long taskId, User user) {
        AgentTask task = findTask(taskId, user);
        if (task.getStatus() != AgentTaskStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException("Task is not waiting for approval.");
        }
        task.setStatus(AgentTaskStatus.RUNNING);
        agentTaskRepository.save(task);
    }

    @Transactional
    public void cancelTask(Long taskId, User user) {
        AgentTask task = findTask(taskId, user);
        if (task.getStatus() != AgentTaskStatus.PENDING
                && task.getStatus() != AgentTaskStatus.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException(
                    "Only pending or approval-waiting tasks can be cancelled.");
        }
        task.setStatus(AgentTaskStatus.CANCELLED);
        task.setCompletedAt(Instant.now());
        agentTaskRepository.save(task);
    }

    private AgentTask findTask(Long taskId, User user) {
        return agentTaskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new AgentTaskNotFoundException(
                        "Agent task not found with id: " + taskId));
    }

    private String safeErrorMessage(RuntimeException exception) {
        return safeErrorMessage(exception.getMessage() == null
                ? exception.getClass().getSimpleName()
                : exception.getMessage());
    }

    private String safeErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Task execution failed.";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }

    private AgentTaskResponse convertToResponse(AgentTask task) {
        return new AgentTaskResponse(
                task.getId(),
                task.getTaskName(),
                task.getDescription(),
                task.getStatus(),
                task.getResultSummary(),
                task.getErrorMessage(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
