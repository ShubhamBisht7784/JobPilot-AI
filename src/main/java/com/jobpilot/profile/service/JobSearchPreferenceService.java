package com.jobpilot.profile.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.profile.dto.JobSearchPreferenceRequest;
import com.jobpilot.profile.dto.JobSearchPreferenceResponse;
import com.jobpilot.profile.entity.JobSearchPreference;
import com.jobpilot.profile.repository.JobSearchPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobSearchPreferenceService {
    private final JobSearchPreferenceRepository repository;

    public JobSearchPreferenceService(JobSearchPreferenceRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public JobSearchPreferenceResponse get(User user) {
        return repository.findByUser(user).map(this::toResponse).orElseGet(() ->
                new JobSearchPreferenceResponse("Java Backend Developer fresher India",
                        "India", false, true, 40, null));
    }

    @Transactional
    public JobSearchPreferenceResponse save(User user, JobSearchPreferenceRequest request) {
        JobSearchPreference preference = repository.findByUser(user).orElseGet(() -> {
            JobSearchPreference created = new JobSearchPreference();
            created.setUser(user);
            return created;
        });
        preference.setSearchKeywords(request.searchKeywords().trim());
        preference.setPreferredLocations(request.preferredLocations() == null
                ? "" : request.preferredLocations().trim());
        preference.setRemoteOnly(request.remoteOnly());
        preference.setAutoDiscoveryEnabled(request.autoDiscoveryEnabled());
        preference.setMinimumMatchScore(request.minimumMatchScore());
        return toResponse(repository.save(preference));
    }

    private JobSearchPreferenceResponse toResponse(JobSearchPreference p) {
        return new JobSearchPreferenceResponse(p.getSearchKeywords(), p.getPreferredLocations(),
                p.isRemoteOnly(), p.isAutoDiscoveryEnabled(), p.getMinimumMatchScore(), p.getUpdatedAt());
    }
}
