
package com.jobpilot.email.entity;

import com.jobpilot.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "gmail_connections",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_gmail_connection_user",
            columnNames = "user_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class GmailConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String gmailAddress;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private Instant accessTokenExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String scopes;

    private Instant connectedAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        connectedAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
