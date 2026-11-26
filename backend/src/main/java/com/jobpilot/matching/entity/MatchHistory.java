package com.jobpilot.matching.entity;

import com.jobpilot.auth.entity.User;
import com.jobpilot.job.entity.Job;
import com.jobpilot.resume.entity.Resume;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matching_history")
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private double matchScore;

    @ElementCollection
    @CollectionTable(
            name = "matched_skills",
            joinColumns = @JoinColumn(name = "matching_history_id")
    )
    @Column(name = "skill")
    private List<String> matchedSkills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "missing_skills",
            joinColumns = @JoinColumn(name = "matching_history_id")
    )
    @Column(name = "skill")
    private List<String> missingSkills = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(length = 40)
    private String matchingMethod = "SKILL_BASED";

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return this.id; }
    public void setId(Long value) { this.id = value; }
    public User getUser() { return this.user; }
    public void setUser(User value) { this.user = value; }
    public Resume getResume() { return this.resume; }
    public void setResume(Resume value) { this.resume = value; }
    public Job getJob() { return this.job; }
    public void setJob(Job value) { this.job = value; }
    public double getMatchScore() { return this.matchScore; }
    public void setMatchScore(double value) { this.matchScore = value; }
    public List<String> getMatchedSkills() { return this.matchedSkills; }
    public void setMatchedSkills(List<String> value) { this.matchedSkills = value; }
    public List<String> getMissingSkills() { return this.missingSkills; }
    public void setMissingSkills(List<String> value) { this.missingSkills = value; }
    public String getMatchingMethod() { return this.matchingMethod; }
    public void setMatchingMethod(String value) { this.matchingMethod = value; }
    public String getRecommendation() { return this.recommendation; }
    public void setRecommendation(String value) { this.recommendation = value; }
    public Instant getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(Instant value) { this.createdAt = value; }
}