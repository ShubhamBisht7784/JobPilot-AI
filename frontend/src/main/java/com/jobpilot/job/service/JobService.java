package com.jobpilot.job.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jobpilot.common.exception.JobNotFoundException;
import com.jobpilot.job.entity.Job;
import com.jobpilot.job.entity.JobSource;
import com.jobpilot.job.entity.JobStatus;
import com.jobpilot.job.repository.JobRepository;

import com.jobpilot.job.dto.JobRequest;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Create a new job
    public Job createJob(Job job) {

        if (jobRepository.existsByJobUrl(job.getJobUrl())) {
            throw new IllegalArgumentException("Job already exists");
        }

        return jobRepository.save(job);
    }

    // Get all jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Get only open jobs
    public List<Job> getOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN);
    }

    // Search jobs by company name
    public List<Job> searchByCompany(String companyName) {
        return jobRepository.findByCompanyNameContainingIgnoreCase(companyName);
    }

    // Get all jobs with pagination and sorting
    public Page<Job> getAllJobs(int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).descending()
        );

        return jobRepository.findAll(pageable);
    }

    // Get open jobs with pagination
    public Page<Job> getOpenJobs(int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).descending()
        );

        return jobRepository.findByStatus(JobStatus.OPEN, pageable);
    }

    // Search company jobs with pagination
    public Page<Job> searchByCompany(
            String companyName,
            int page,
            int size,
            String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).descending()
        );

        return jobRepository.findByCompanyNameContainingIgnoreCase(
                companyName,
                pageable
        );
    }

    public Job getJobById(Long id) {

        return jobRepository.findById(id)
                .orElseThrow(()
                        -> new JobNotFoundException(
                                "Job not found with id: " + id
                        )
                );
    }

    public Job updateJob(Long id, JobRequest request) {

        // Check whether job exists
        Job existingJob = jobRepository.findById(id)
                .orElseThrow(()
                        -> new JobNotFoundException(
                                "Job not found with id: " + id
                        )
                );

        // Check duplicate URL excluding current job
        if (jobRepository.existsByJobUrlAndIdNot(
                request.getJobUrl(), id)) {

            throw new IllegalArgumentException(
                    "Another job already exists with this URL"
            );
        }

        // Update job fields
        existingJob.setTitle(request.getTitle());
        existingJob.setCompanyName(request.getCompanyName());
        existingJob.setLocation(request.getLocation());
        existingJob.setDescription(request.getDescription());
        existingJob.setJobUrl(request.getJobUrl());
        existingJob.setSource(request.getSource());
        existingJob.setPostedAt(request.getPostedAt());

        return jobRepository.save(existingJob);
    }

    public void deleteJob(Long id) {

        Job existingJob = jobRepository.findById(id)
                .orElseThrow(()
                        -> new JobNotFoundException(
                                "Job not found with id: " + id
                        )
                );

        jobRepository.delete(existingJob);
    }

    public Page<Job> searchByTitle(
            String title,
            int page,
            int size,
            String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).descending()
        );

        return jobRepository.findByTitleContainingIgnoreCase(
                title,
                pageable
        );
    }

    public Page<Job> searchByLocation(
            String location,
            int page,
            int size,
            String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).descending()
        );

        return jobRepository.findByLocationContainingIgnoreCase(
                location,
                pageable
        );
    }

    public Page<Job> searchBySource(
            JobSource source,
            int page,
            int size,
            String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).descending()
        );

        return jobRepository.findBySource(source, pageable);
    }

}
