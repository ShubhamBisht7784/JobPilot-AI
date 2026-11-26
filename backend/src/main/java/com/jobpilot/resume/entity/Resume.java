
package com.jobpilot.resume.entity;

import com.jobpilot.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    public void onCreate() {
        uploadedAt = Instant.now();
    }
}
