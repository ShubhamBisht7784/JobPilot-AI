package com.jobpilot.approval.entity;

import com.jobpilot.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "approvals")
@Getter
@Setter
@NoArgsConstructor
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant createdAt;

    private Instant decidedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalExecutionStatus executionStatus;

    @Column(columnDefinition = "TEXT")
    private String executionError;

    @Column(nullable = false)
private Integer executionAttempts = 0;

private Instant executionStartedAt;

private Instant executionCompletedAt;

@Version
private Long version;

    @PrePersist
    public void onCreate() {

        if (executionAttempts == null) {
    executionAttempts = 0;
}
        if (status == null) {
            status = ApprovalStatus.PENDING;
        }

        if (executionStatus == null) {
            executionStatus = ApprovalExecutionStatus.NOT_STARTED;
        }

        createdAt = Instant.now();
    }
}
