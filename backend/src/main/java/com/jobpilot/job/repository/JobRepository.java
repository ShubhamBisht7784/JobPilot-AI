package com.jobpilot.job.repository;

import com.jobpilot.job.entity.Job;
import com.jobpilot.job.entity.JobSource;
import com.jobpilot.job.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByJobUrl(String jobUrl);

    boolean existsByDeduplicationKey(String deduplicationKey);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByCompanyNameContainingIgnoreCase(String companyName);


// Paginated search by status
Page<Job> findByStatus(JobStatus status, Pageable pageable);

// Paginated search by company
Page<Job> findByCompanyNameContainingIgnoreCase(
        String companyName, Pageable pageable
);

boolean existsByJobUrlAndIdNot(String jobUrl, Long id);

Page<Job> findByTitleContainingIgnoreCase(
        String title,
        Pageable pageable
);


Page<Job> findByLocationContainingIgnoreCase(
        String location,
        Pageable pageable
);



Page<Job> findBySource(
        JobSource source,
        Pageable pageable
);


}