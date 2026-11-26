package com.jobpilot.application.entity;

import java.time.Instant;

import com.jobpilot.auth.entity.User;
import com.jobpilot.job.entity.Job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private Instant appliedAt;

    private Instant lastUpdatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Instant nextFollowUpAt;

    @PrePersist
    public void onCreate() {

        if (status == null) {
            status = ApplicationStatus.SAVED;
        }

        Instant now = Instant.now();

        if (status == ApplicationStatus.APPLIED) {
            appliedAt = now;
        }

        lastUpdatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        lastUpdatedAt = Instant.now();
    }
}
