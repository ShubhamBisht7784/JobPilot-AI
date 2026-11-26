
package com.jobpilot.job.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String companyName;

    private String location;

    @Column(length = 160)
    private String sourceName;

    @Column(length = 300)
    private String salary;

    @Column(length = 120)
    private String employmentType;

    @Column(length = 80)
    private String workMode;

    @Column(unique = true, length = 64)
    private String deduplicationKey;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 1000)
    private String jobUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private Instant postedAt;

    @Column(nullable = false, updatable = false)
    private Instant discoveredAt;

    @PrePersist
    public void onCreate() {
        discoveredAt = Instant.now();

        if (status == null) {
            status = JobStatus.OPEN;
        }
    }

    public Long getId() { return this.id; }
    public void setId(Long value) { this.id = value; }
    public String getTitle() { return this.title; }
    public void setTitle(String value) { this.title = value; }
    public String getCompanyName() { return this.companyName; }
    public void setCompanyName(String value) { this.companyName = value; }
    public String getLocation() { return this.location; }
    public void setLocation(String value) { this.location = value; }
    public String getSourceName() { return this.sourceName; }
    public void setSourceName(String value) { this.sourceName = value; }
    public String getSalary() { return this.salary; }
    public void setSalary(String value) { this.salary = value; }
    public String getEmploymentType() { return this.employmentType; }
    public void setEmploymentType(String value) { this.employmentType = value; }
    public String getWorkMode() { return this.workMode; }
    public void setWorkMode(String value) { this.workMode = value; }
    public String getDeduplicationKey() { return this.deduplicationKey; }
    public void setDeduplicationKey(String value) { this.deduplicationKey = value; }
    public String getDescription() { return this.description; }
    public void setDescription(String value) { this.description = value; }
    public String getJobUrl() { return this.jobUrl; }
    public void setJobUrl(String value) { this.jobUrl = value; }
    public JobSource getSource() { return this.source; }
    public void setSource(JobSource value) { this.source = value; }
    public JobStatus getStatus() { return this.status; }
    public void setStatus(JobStatus value) { this.status = value; }
    public Instant getPostedAt() { return this.postedAt; }
    public void setPostedAt(Instant value) { this.postedAt = value; }
    public Instant getDiscoveredAt() { return this.discoveredAt; }
    public void setDiscoveredAt(Instant value) { this.discoveredAt = value; }
}
