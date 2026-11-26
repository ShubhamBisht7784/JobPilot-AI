package com.jobpilot.matching.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.common.exception.JobNotFoundException;
import com.jobpilot.common.exception.MatchHistoryNotFoundException;
import com.jobpilot.common.exception.ResumeNotFoundException;
import com.jobpilot.job.entity.Job;
import com.jobpilot.job.repository.JobRepository;
import com.jobpilot.matching.dto.MatchHistoryResponse;
import com.jobpilot.matching.dto.MatchingResponse;
import com.jobpilot.resume.entity.Resume;
import com.jobpilot.resume.repository.ResumeRepository;

import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.jobpilot.matching.entity.MatchHistory;
import com.jobpilot.matching.repository.MatchHistoryRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillExtractorService skillExtractorService;
    private final MatchHistoryRepository matchHistoryRepository;
    private final AiMatchScoreService aiMatchScoreService;

    public MatchingService(
            ResumeRepository resumeRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            SkillExtractorService skillExtractorService,
            MatchHistoryRepository matchHistoryRepository,
            AiMatchScoreService aiMatchScoreService
    ) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.skillExtractorService = skillExtractorService;
        this.matchHistoryRepository = matchHistoryRepository;
        this.aiMatchScoreService = aiMatchScoreService;
    }

    private Job getJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(()
                        -> new JobNotFoundException("Job not found with id: " + jobId)
                );
    }

    @Transactional
    public MatchingResponse matchResumeToJob(
            Long resumeId,
            Long jobId,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return matchResumeToJob(resumeId, jobId, user);
    }

    @Transactional
    public MatchingResponse matchResumeToJob(
            Long resumeId,
            Long jobId,
            User user) {

        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));

        Job job = getJob(jobId);

        var previousMatch = matchHistoryRepository.findByUserAndResumeAndJob(user, resume, job);
        if (previousMatch.isPresent()) {
            MatchHistory existing = previousMatch.get();
            return new MatchingResponse(resumeId, jobId, existing.getMatchScore(),
                    existing.getMatchedSkills(), existing.getMissingSkills(),
                    existing.getRecommendation());
        }

        List<String> resumeSkills
                = skillExtractorService.extractSkills(resume.getExtractedText());

        String jobEvidence = (job.getTitle() == null ? "" : job.getTitle()) + "\n"
                + (job.getDescription() == null ? "" : job.getDescription());
        List<String> jobSkills = skillExtractorService.extractSkills(jobEvidence);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        Set<String> normalizedResumeSkills = resumeSkills.stream()
                .map(this::normalizeSkill)
                .collect(Collectors.toCollection(HashSet::new));

        for (String skill : jobSkills) {

            if (normalizedResumeSkills.contains(normalizeSkill(skill))) {
                matchedSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }
        double skillScore = jobSkills.isEmpty() ? 0.0 : (matchedSkills.size() * 100.0) / jobSkills.size();
        AiMatchScoreService.Score aiScore = aiMatchScoreService.score(resume, job, skillScore);
        double matchScore = aiScore.value();

        String recommendation;

        if (matchScore >= 80) {
            recommendation = "Excellent match";
        } else if (matchScore >= 60) {
            recommendation = "Good match";
        } else if (matchScore >= 40) {
            recommendation = "Moderate match";
        } else {
            recommendation = "Low match - improve your required skills";
        }
        MatchHistory history = new MatchHistory();

        history.setUser(resume.getUser());
        history.setResume(resume);
        history.setJob(job);
        history.setMatchScore(matchScore);
        history.setMatchedSkills(matchedSkills);
        history.setMissingSkills(missingSkills);
        history.setRecommendation(recommendation);
        history.setMatchingMethod(aiScore.method());

        matchHistoryRepository.save(history);
        return new MatchingResponse(
                resumeId,
                jobId,
                Math.round(matchScore * 100.0) / 100.0,
                matchedSkills,
                missingSkills,
                recommendation
        );
    }

    private String normalizeSkill(String skill) {
        return skill.trim().toLowerCase();
    }

    @Transactional(readOnly = true)
    public Page<MatchHistoryResponse> getMatchingHistory(
            Authentication authentication,
            int page,
            int size
    ) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found")
                );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<MatchHistory> historyPage
                = matchHistoryRepository.findByUser(user, pageable);

        return historyPage.map(history -> new MatchHistoryResponse(
                history.getId(),
                history.getResume().getId(),
                history.getResume().getFileName(),
                history.getJob().getId(),
                history.getJob().getTitle(),
                history.getJob().getCompanyName(),
                history.getMatchScore(),
                history.getMatchedSkills(),
                history.getMissingSkills(),
                history.getRecommendation(),
                history.getCreatedAt()
        ));
    }

    @Transactional(readOnly = true)
    public MatchHistoryResponse getMatchingHistoryById(
            Long historyId,
            Authentication authentication
    ) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new IllegalArgumentException("User not found")
                );

        MatchHistory history = matchHistoryRepository
                .findByIdAndUser(historyId, user)
                .orElseThrow(()
                        -> new MatchHistoryNotFoundException(
                                "Matching history not found with id: " + historyId
                        )
                );

        return new MatchHistoryResponse(
                history.getId(),
                history.getResume().getId(),
                history.getResume().getFileName(),
                history.getJob().getId(),
                history.getJob().getTitle(),
                history.getJob().getCompanyName(),
                history.getMatchScore(),
                history.getMatchedSkills(),
                history.getMissingSkills(),
                history.getRecommendation(),
                history.getCreatedAt()
        );
    }

    @Transactional
public void deleteMatchingHistory(
        Long historyId,
        Authentication authentication
) {
    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found")
            );

    MatchHistory history = matchHistoryRepository
            .findByIdAndUser(historyId, user)
            .orElseThrow(() ->
                    new MatchHistoryNotFoundException(
                            "Matching history not found with id: " + historyId
                    )
            );

    matchHistoryRepository.delete(history);
}
}
