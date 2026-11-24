package com.jobpilot.job.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.profile.entity.JobSearchPreference;
import com.jobpilot.profile.repository.JobSearchPreferenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(name = "app.job-discovery.enabled", havingValue = "true", matchIfMissing = false)
public class JobDiscoveryScheduler {
    private static final Logger log = LoggerFactory.getLogger(JobDiscoveryScheduler.class);
    private final JobDiscoveryService jobDiscoveryService;
    private final UserRepository userRepository;
    private final JobSearchPreferenceRepository preferenceRepository;
    private final JobRecommendationService recommendationService;

    public JobDiscoveryScheduler(JobDiscoveryService jobDiscoveryService, UserRepository userRepository,
            JobSearchPreferenceRepository preferenceRepository, JobRecommendationService recommendationService) {
        this.jobDiscoveryService = jobDiscoveryService;
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.recommendationService = recommendationService;
    }

    @Value("${app.job-discovery.queries:}")
    private String configuredQueries;

    @Value("${app.serpapi.api-key:}")
    private String serpApiKey;

    @Scheduled(fixedDelayString = "${app.job-discovery.fixed-delay-ms:21600000}",
            initialDelayString = "${app.job-discovery.initial-delay-ms:60000}")
    public void discoverAndMatchJobs() {
        if (serpApiKey == null || serpApiKey.isBlank()) {
            log.warn("Automatic job discovery skipped: configure SERPAPI_API_KEY first.");
            return;
        }

        // Global baseline searches keep discovery useful before a user saves preferences.
        runQueries(configuredQueries, "global");

        // User-specific searches use the user's saved keywords and locations.
        for (JobSearchPreference preference : preferenceRepository.findByAutoDiscoveryEnabledTrue()) {
            runPreferenceQueries(preference);
        }

        // Match every newly discovered job against each user's latest uploaded resume.
        for (User user : userRepository.findAll()) {
            try {
                int matched = recommendationService.scoreNewJobsForUser(user);
                if (matched > 0) log.info("Automatic matching completed for userId={}, newMatches={}", user.getId(), matched);
            } catch (Exception exception) {
                log.warn("Automatic matching failed for userId={}: {}", user.getId(), exception.getClass().getSimpleName());
            }
        }
    }

    private void runQueries(String rawQueries, String owner) {
        if (rawQueries == null || rawQueries.isBlank()) return;
        for (String query : rawQueries.split("[,;]")) {
            runSingleQuery(query.trim(), owner);
        }
    }

    private void runPreferenceQueries(JobSearchPreference preference) {
        String[] keywords = preference.getSearchKeywords().split("[,;]");
        String locationsText = preference.getPreferredLocations();
        String[] locations = locationsText == null || locationsText.isBlank()
                ? new String[]{""} : locationsText.split("[,;]");
        String owner = "userId=" + preference.getUser().getId();
        for (String keyword : keywords) {
            for (String location : locations) {
                String query = withLocations(keyword, location, preference.isRemoteOnly());
                runSingleQuery(query, owner);
            }
        }
    }

    private void runSingleQuery(String query, String owner) {
        if (query == null || query.isBlank()) return;
        try {
            var result = jobDiscoveryService.discover(query);
            log.info("Job discovery owner={}, query='{}', found={}, saved={}, duplicates={}",
                    owner, query, result.jobsFound(), result.jobsSaved(), result.duplicatesSkipped());
        } catch (Exception exception) {
            log.warn("Job discovery failed owner={}, query='{}': {}", owner, query,
                    exception.getClass().getSimpleName());
        }
    }

    private String withLocations(String keywords, String locations, boolean remoteOnly) {
        StringBuilder query = new StringBuilder(keywords == null ? "" : keywords.trim());
        if (locations != null && !locations.isBlank()) query.append(" ").append(locations.trim());
        if (remoteOnly) query.append(" remote");
        return query.toString();
    }
}
