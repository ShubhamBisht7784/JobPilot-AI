package com.jobpilot.job.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.common.exception.ResumeNotFoundException;
import com.jobpilot.job.dto.JobRecommendationResponse;
import com.jobpilot.job.entity.JobStatus;
import com.jobpilot.job.repository.JobRepository;
import com.jobpilot.matching.entity.MatchHistory;
import com.jobpilot.matching.repository.MatchHistoryRepository;
import com.jobpilot.matching.service.MatchingService;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.repository.ResumeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobRecommendationService {
    private final ResumeRepository resumeRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final MatchingService matchingService;
    private final JobRepository jobRepository;

    public JobRecommendationService(ResumeRepository resumeRepository, MatchHistoryRepository matchHistoryRepository,
            MatchingService matchingService, JobRepository jobRepository) {
        this.resumeRepository = resumeRepository;
        this.matchHistoryRepository = matchHistoryRepository;
        this.matchingService = matchingService;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public int scoreNewJobsForUser(User user) {
        Resume resume = resumeRepository.findFirstByUserOrderByUploadedAtDesc(user)
                .orElse(null);
        if (resume == null) return 0;

        // The scheduler calls this after each discovery run. Only unscored jobs are processed.
        // Keep each run bounded so a large historical jobs table cannot monopolize a scheduler thread.
        int processed = 0;
        var jobs = jobRepository.findByStatus(JobStatus.OPEN,
                PageRequest.of(0, 250, Sort.by(Sort.Direction.DESC, "discoveredAt"))).getContent();
        for (var job : jobs) {
            if (matchHistoryRepository.findByUserAndResumeAndJob(user, resume, job).isPresent()) continue;
            try {
                matchingService.matchResumeToJob(resume.getId(), job.getId(), user);
                processed++;
            } catch (IllegalArgumentException ignored) {
                // A job without extractable skills or an empty resume is not matchable yet.
            }
        }
        return processed;
    }

    @Transactional(readOnly = true)
    public Page<JobRecommendationResponse> getRecommendations(User user, double minimumScore,
            int page, int size) {
        Resume resume = resumeRepository.findFirstByUserOrderByUploadedAtDesc(user)
                .orElseThrow(() -> new ResumeNotFoundException(
                        "Upload a PDF resume before requesting personalized job recommendations."));
        Pageable pageable = PageRequest.of(page, size);
        return matchHistoryRepository
                .findByUserAndResumeAndMatchScoreGreaterThanEqualOrderByMatchScoreDescCreatedAtDesc(
                        user, resume, minimumScore, pageable)
                .map(this::toResponse);
    }

    private JobRecommendationResponse toResponse(MatchHistory match) {
        var job = match.getJob();
        return new JobRecommendationResponse(job.getId(), job.getTitle(), job.getCompanyName(),
                job.getLocation(), job.getSourceName(), job.getSalary(), job.getEmploymentType(),
                job.getWorkMode(), job.getDescription(), job.getJobUrl(), job.getSource(),
                job.getPostedAt(), job.getDiscoveredAt(), match.getMatchScore(),
                match.getMatchedSkills(), match.getMissingSkills(), match.getRecommendation(),
                match.getMatchingMethod());
    }
}
