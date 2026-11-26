package com.jobpilot.job.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobpilot.job.dto.JobRequest;
import com.jobpilot.job.dto.JobResponse;
import com.jobpilot.job.entity.Job;
import com.jobpilot.job.entity.JobSource;
import com.jobpilot.job.service.JobService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request) {

        Job job = new Job();

        job.setTitle(request.getTitle());
        job.setCompanyName(request.getCompanyName());
        job.setLocation(request.getLocation());
        job.setDescription(request.getDescription());
        job.setJobUrl(request.getJobUrl());
        job.setSource(request.getSource());
        job.setPostedAt(request.getPostedAt());

        Job savedJob = jobService.createJob(job);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertToResponse(savedJob));
    }

    private static final int MAX_PAGE_SIZE = 50;

// Get all jobs with pagination
    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy) {

        validatePagination(page, size, sortBy);

        Page<JobResponse> response = jobService
                .getAllJobs(page, size, sortBy)
                .map(this::convertToResponse);

        return ResponseEntity.ok(response);
    }

// Get open jobs with pagination
    @GetMapping("/open")
    public ResponseEntity<Page<JobResponse>> getOpenJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy) {

        validatePagination(page, size, sortBy);

        Page<JobResponse> response = jobService
                .getOpenJobs(page, size, sortBy)
                .map(this::convertToResponse);

        return ResponseEntity.ok(response);
    }

// Search jobs by company with pagination
    @GetMapping("/search")
    public ResponseEntity<Page<JobResponse>> searchByCompany(
            @RequestParam String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy) {

        validatePagination(page, size, sortBy);

        Page<JobResponse> response = jobService
                .searchByCompany(company, page, size, sortBy)
                .map(this::convertToResponse);

        return ResponseEntity.ok(response);
    }

    private void validatePagination(int page, int size, String sortBy) {

        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and 50");
        }

        if (!sortBy.equals("postedAt") && !sortBy.equals("discoveredAt")) {
            throw new IllegalArgumentException("Invalid sorting field");
        }

    }

    @SuppressWarnings("unused")
    private JobResponse convertToResponse(Job job) {

        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getCompanyName(),
                job.getLocation(),
                job.getSourceName(),
                job.getSalary(),
                job.getEmploymentType(),
                job.getWorkMode(),
                job.getDescription(),
                job.getJobUrl(),
                job.getSource(),
                job.getStatus(),
                job.getPostedAt(),
                job.getDiscoveredAt()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long id) {

        Job job = jobService.getJobById(id);

        return ResponseEntity.ok(convertToResponse(job));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request) {

        Job updatedJob = jobService.updateJob(id, request);

        return ResponseEntity.ok(
                convertToResponse(updatedJob)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {

        jobService.deleteJob(id);

        return ResponseEntity.noContent().build();
    }


@GetMapping("/search/title")
public Page<JobResponse> searchByTitle(
        @RequestParam String title,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "postedAt") String sortBy) {

    validatePagination(page, size, sortBy);

    return jobService.searchByTitle(title, page, size, sortBy)
            .map(this::convertToResponse);
}


@GetMapping("/search/location")
public Page<JobResponse> searchByLocation(
        @RequestParam String location,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "postedAt") String sortBy) {

    validatePagination(page, size, sortBy);

    return jobService.searchByLocation(location, page, size, sortBy)
            .map(this::convertToResponse);
}


@GetMapping("/search/source")
public Page<JobResponse> searchBySource(
        @RequestParam JobSource source,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "postedAt") String sortBy) {

    validatePagination(page, size, sortBy);

    return jobService.searchBySource(source, page, size, sortBy)
            .map(this::convertToResponse);
}


}
