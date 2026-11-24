package com.jobpilot.application.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.jobpilot.application.entity.Application;
import com.jobpilot.application.entity.ApplicationStatus;
import com.jobpilot.auth.entity.User;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUser(User user);

    @EntityGraph(attributePaths = "job")
    Optional<Application> findByIdAndUser(Long id, User user);

    boolean existsByUserAndJobId(User user, Long jobId);

    Page<Application> findByUser(User user, Pageable pageable);

    Page<Application> findByUserAndStatus(
            User user,
            ApplicationStatus status,
            Pageable pageable
    );

    long countByUser(User user);

    long countByUserAndStatus(
            User user,
            ApplicationStatus status
    );


    List<Application> findByUserAndNextFollowUpAtLessThanEqualOrderByNextFollowUpAtAsc(
        User user,
        Instant dateTime
);

    List<Application> findByUserAndNextFollowUpAtGreaterThanOrderByNextFollowUpAtAsc(
        User user,
        Instant dateTime
);
}
    
            
            
    