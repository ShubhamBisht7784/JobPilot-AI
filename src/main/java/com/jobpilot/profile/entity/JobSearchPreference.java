package com.jobpilot.profile.entity;

import com.jobpilot.auth.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "job_search_preferences", uniqueConstraints =
        @UniqueConstraint(name = "uk_job_search_preferences_user", columnNames = "user_id"))
public class JobSearchPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 2000)
    private String searchKeywords = "Java Backend Developer fresher India";

    @Column(length = 1000)
    private String preferredLocations = "India";

    @Column(nullable = false)
    private boolean remoteOnly;

    @Column(nullable = false)
    private boolean autoDiscoveryEnabled = true;

    @Column(nullable = false)
    private int minimumMatchScore = 40;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(String value) { this.searchKeywords = value; }
    public String getPreferredLocations() { return preferredLocations; }
    public void setPreferredLocations(String value) { this.preferredLocations = value; }
    public boolean isRemoteOnly() { return remoteOnly; }
    public void setRemoteOnly(boolean value) { this.remoteOnly = value; }
    public boolean isAutoDiscoveryEnabled() { return autoDiscoveryEnabled; }
    public void setAutoDiscoveryEnabled(boolean value) { this.autoDiscoveryEnabled = value; }
    public int getMinimumMatchScore() { return minimumMatchScore; }
    public void setMinimumMatchScore(int value) { this.minimumMatchScore = value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { this.updatedAt = value; }
}
