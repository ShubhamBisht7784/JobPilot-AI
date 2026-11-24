package com.jobpilot.matching.repository;

import com.jobpilot.auth.entity.User;
import com.jobpilot.matching.entity.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatchHistoryRepository
        extends JpaRepository<MatchHistory, Long> {

    Page<MatchHistory> findByUser(
            User user,
            Pageable pageable
    );

    Optional<MatchHistory> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);

    java.util.Optional<MatchHistory> findByUserAndResumeAndJob(User user,
            com.jobpilot.resume.entity.Resume resume,
            com.jobpilot.job.entity.Job job);

    Page<MatchHistory> findByUserAndResumeOrderByMatchScoreDescCreatedAtDesc(
            User user, com.jobpilot.resume.entity.Resume resume, Pageable pageable);

    Page<MatchHistory> findByUserAndResumeAndMatchScoreGreaterThanEqualOrderByMatchScoreDescCreatedAtDesc(
            User user, com.jobpilot.resume.entity.Resume resume, double minimumScore, Pageable pageable);

}
