package com.jobpilot.recruiter.entity;

import com.jobpilot.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "recruiters")
@Getter
@Setter
@NoArgsConstructor
public class Recruiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 150)
    private String designation;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(length = 500)
    private String linkedinUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(name = "email_verification_score")
    private Integer emailVerificationScore;

    @Column(name = "email_verification_status", length = 30)
    private String emailVerificationStatus;

    @Column(name = "email_provenance", length = 50)
    private String emailProvenance;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
