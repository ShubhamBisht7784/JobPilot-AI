
package com.jobpilot.email.entity;

import com.jobpilot.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "emails")
@Getter
@Setter
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String recipientEmail;

    @Column(length = 255)
    private String recipientName;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailStatus status;

    @Column(length = 500)
    private String gmailMessageId;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private Instant createdAt;

    private Instant sentAt;

    @Column(unique = true)
private Long approvalId;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = EmailStatus.DRAFT;
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
