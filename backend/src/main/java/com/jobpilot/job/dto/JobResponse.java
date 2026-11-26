
package com.jobpilot.job.dto;

import java.time.Instant;

import com.jobpilot.job.entity.JobSource;
import com.jobpilot.job.entity.JobStatus;


public class JobResponse {

    private Long id;
    private String title;
    private String companyName;
    private String location;
    private String sourceName;
    private String salary;
    private String employmentType;
    private String workMode;
    private String description;
    private String jobUrl;
    private JobSource source;
    private JobStatus status;
    private Instant postedAt;
    private Instant discoveredAt;

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
    public JobResponse(Long id, String title, String companyName, String location, String sourceName, String salary, String employmentType, String workMode, String description, String jobUrl, JobSource source, JobStatus status, Instant postedAt, Instant discoveredAt) { this.id = id; this.title = title; this.companyName = companyName; this.location = location; this.sourceName = sourceName; this.salary = salary; this.employmentType = employmentType; this.workMode = workMode; this.description = description; this.jobUrl = jobUrl; this.source = source; this.status = status; this.postedAt = postedAt; this.discoveredAt = discoveredAt; }
}
