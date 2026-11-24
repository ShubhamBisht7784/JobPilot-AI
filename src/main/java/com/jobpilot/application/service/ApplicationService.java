package com.jobpilot.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobpilot.application.dto.ApplicationRequest;
import com.jobpilot.application.dto.ApplicationResponse;
import com.jobpilot.application.dto.ApplicationStatsResponse;
import com.jobpilot.application.entity.Application;
import com.jobpilot.application.entity.ApplicationStatus;
import com.jobpilot.application.repository.ApplicationRepository;
import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.job.entity.Job;
import com.jobpilot.job.repository.JobRepository;
import com.jobpilot.application.dto.ApplicationFollowUpRequest;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public ApplicationResponse createApplication(
            ApplicationRequest request,
            Authentication authentication) {

        User user = getUser(authentication);

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(()
                        -> new IllegalArgumentException("Job not found"));

        if (applicationRepository.existsByUserAndJobId(
                user, job.getId())) {

            throw new IllegalArgumentException(
                    "You have already saved this job");
        }

        Application application = new Application();

        application.setUser(user);
        application.setJob(job);

        application.setStatus(
                request.getStatus() != null
                ? request.getStatus()
                : ApplicationStatus.SAVED
        );

        application.setNotes(request.getNotes());

        Application savedApplication
                = applicationRepository.save(application);

        return convertToResponse(savedApplication);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(
            Long applicationId,
            ApplicationStatus newStatus,
            Authentication authentication) {

        User user = getUser(authentication);

        Application application = applicationRepository
                .findByIdAndUser(applicationId, user)
                .orElseThrow(()
                        -> new IllegalArgumentException("Application not found"));

        validateStatusTransition(application.getStatus(), newStatus);

        if (newStatus == ApplicationStatus.APPLIED
                && application.getAppliedAt() == null) {

            application.setAppliedAt(java.time.Instant.now());
        }

        if (newStatus == ApplicationStatus.OFFER
                || newStatus == ApplicationStatus.REJECTED
                || newStatus == ApplicationStatus.WITHDRAWN) {

            application.setNextFollowUpAt(null);
        }
        application.setStatus(newStatus);
        Application updatedApplication
                = applicationRepository.save(application);

        return convertToResponse(updatedApplication);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getUserApplications(
            Authentication authentication) {

        User user = getUser(authentication);

        return applicationRepository.findByUser(user)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(
            Long applicationId,
            Authentication authentication) {

        User user = getUser(authentication);

        Application application = applicationRepository
                .findByIdAndUser(applicationId, user)
                .orElseThrow(()
                        -> new IllegalArgumentException("Application not found"));

        return convertToResponse(application);
    }

    private User getUser(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found"));
    }

    private void validateStatusTransition(
            ApplicationStatus currentStatus,
            ApplicationStatus newStatus) {

        boolean isValid = switch (currentStatus) {

            case SAVED ->
                newStatus == ApplicationStatus.APPLIED
                || newStatus == ApplicationStatus.WITHDRAWN;

            case APPLIED ->
                newStatus == ApplicationStatus.INTERVIEW
                || newStatus == ApplicationStatus.REJECTED
                || newStatus == ApplicationStatus.WITHDRAWN;

            case INTERVIEW ->
                newStatus == ApplicationStatus.OFFER
                || newStatus == ApplicationStatus.REJECTED
                || newStatus == ApplicationStatus.WITHDRAWN;

            case OFFER, REJECTED, WITHDRAWN ->
                false;
        };

        if (!isValid) {
            throw new IllegalArgumentException(
                    "Invalid application status transition: "
                    + currentStatus + " -> " + newStatus
            );
        }
    }

    private ApplicationResponse convertToResponse(Application application) {

        Job job = application.getJob();

        ApplicationResponse response = new ApplicationResponse(
                application.getId(),
                job.getId(),
                job.getTitle(),
                job.getCompanyName(),
                job.getLocation(),
                job.getJobUrl(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getLastUpdatedAt(),
                application.getNotes(),
                application.getNextFollowUpAt()
        );

        return response;
    }

    @Transactional
    public void deleteApplication(
            Long applicationId,
            Authentication authentication) {

        User user = getUser(authentication);

        Application application = applicationRepository
                .findByIdAndUser(applicationId, user)
                .orElseThrow(()
                        -> new IllegalArgumentException("Application not found"));

        applicationRepository.delete(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationNotes(
            Long applicationId,
            String notes,
            Authentication authentication) {

        User user = getUser(authentication);

        Application application = applicationRepository
                .findByIdAndUser(applicationId, user)
                .orElseThrow(()
                        -> new IllegalArgumentException("Application not found"));

        application.setNotes(notes);

        Application updatedApplication
                = applicationRepository.save(application);

        return convertToResponse(updatedApplication);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getUserApplications(
            Authentication authentication,
            int page,
            int size,
            ApplicationStatus status) {

        User user = getUser(authentication);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "lastUpdatedAt")
        );

        Page<Application> applications;

        if (status != null) {
            applications = applicationRepository.findByUserAndStatus(
                    user,
                    status,
                    pageable
            );
        } else {
            applications = applicationRepository.findByUser(
                    user,
                    pageable
            );
        }

        return applications.map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public ApplicationStatsResponse getApplicationStats(
            Authentication authentication) {

        User user = getUser(authentication);

        long totalApplications
                = applicationRepository.countByUser(user);

        long saved = applicationRepository.countByUserAndStatus(
                user, ApplicationStatus.SAVED);

        long applied = applicationRepository.countByUserAndStatus(
                user, ApplicationStatus.APPLIED);

        long interviews = applicationRepository.countByUserAndStatus(
                user, ApplicationStatus.INTERVIEW);

        long offers = applicationRepository.countByUserAndStatus(
                user, ApplicationStatus.OFFER);

        long rejected = applicationRepository.countByUserAndStatus(
                user, ApplicationStatus.REJECTED);

        long withdrawn = applicationRepository.countByUserAndStatus(
                user, ApplicationStatus.WITHDRAWN);

        return new ApplicationStatsResponse(
                totalApplications,
                saved,
                applied,
                interviews,
                offers,
                rejected,
                withdrawn
        );
    }

    @Transactional
    public ApplicationResponse updateApplicationFollowUp(
            Long applicationId,
            ApplicationFollowUpRequest request,
            Authentication authentication) {

        User user = getUser(authentication);

        Application application = applicationRepository
                .findByIdAndUser(applicationId, user)
                .orElseThrow(()
                        -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() == ApplicationStatus.OFFER
                || application.getStatus() == ApplicationStatus.REJECTED
                || application.getStatus() == ApplicationStatus.WITHDRAWN) {

            throw new IllegalArgumentException(
                    "Cannot schedule follow-up for a closed application"
            );
        }

        application.setNextFollowUpAt(request.getNextFollowUpAt());

        Application updatedApplication = applicationRepository.save(application);

        return convertToResponse(updatedApplication);
    }

    public List<ApplicationResponse> getDueFollowUps(Authentication authentication) {

        User user = getUser(authentication);

        List<Application> applications = applicationRepository
                .findByUserAndNextFollowUpAtLessThanEqualOrderByNextFollowUpAtAsc(
                        user,
                        Instant.now()
                );

        return applications.stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<ApplicationResponse> getUpcomingFollowUps(
            Authentication authentication) {

        User user = getUser(authentication);

        List<Application> applications = applicationRepository
                .findByUserAndNextFollowUpAtGreaterThanOrderByNextFollowUpAtAsc(
                        user,
                        Instant.now()
                );

        return applications.stream()
                .map(this::convertToResponse)
                .toList();
    }
}
