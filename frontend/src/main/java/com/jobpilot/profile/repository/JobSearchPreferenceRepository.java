package com.jobpilot.profile.repository;

import com.jobpilot.auth.entity.User;
import com.jobpilot.profile.entity.JobSearchPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobSearchPreferenceRepository extends JpaRepository<JobSearchPreference, Long> {
    Optional<JobSearchPreference> findByUser(User user);
    List<JobSearchPreference> findByAutoDiscoveryEnabledTrue();
}
