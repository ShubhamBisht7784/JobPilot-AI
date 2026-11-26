package com.jobpilot.recruiter.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.recruiter.dto.BulkRecruiterDiscoveryResponse;
import com.jobpilot.recruiter.dto.RecruiterBulkDiscoveryRequest;
import com.jobpilot.recruiter.dto.RecruiterDiscoveryFailure;
import com.jobpilot.recruiter.dto.RecruiterEmailResult;

import com.jobpilot.recruiter.dto.RecruiterResponse;
import com.jobpilot.recruiter.dto.RecruiterSearchRequest;
import com.jobpilot.recruiter.dto.RecruiterSearchResult;
import com.jobpilot.recruiter.integration.HunterApiException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BulkRecruiterDiscoveryService {

    private final RecruiterSearchService recruiterSearchService;
    private final RecruiterEmailService recruiterEmailService;

    public BulkRecruiterDiscoveryResponse discoverAndSaveRecruiters(
            RecruiterBulkDiscoveryRequest request,
            User user) {

        int maxProfiles = request.maxProfiles() == null
                ? 3
                : request.maxProfiles();

        RecruiterSearchRequest searchRequest = new RecruiterSearchRequest(
                request.companyName(),
                request.designation(),
                request.location()
        );

        List<RecruiterSearchResult> searchResults
                = recruiterSearchService.searchRecruiters(searchRequest);

                Set<String> uniqueProfileUrls = new LinkedHashSet<>();

for (RecruiterSearchResult result : searchResults) {

    String linkedinUrl = result.profileUrl();

    if (linkedinUrl != null && !linkedinUrl.isBlank()) {
        uniqueProfileUrls.add(linkedinUrl);
    }
}

int totalProfilesFound = uniqueProfileUrls.size();

        List<RecruiterResponse> savedRecruiters = new ArrayList<>();

        List<RecruiterDiscoveryFailure> failures = new ArrayList<>();

        Set<String> processedProfiles = new LinkedHashSet<>();
        Set<Long> returnedRecruiterIds = new LinkedHashSet<>();

        int profilesProcessed = 0;
        int emailsFound = 0;

        for (RecruiterSearchResult searchResult : searchResults) {

            if (profilesProcessed >= maxProfiles) {
                break;
            }

            String linkedinUrl = searchResult.profileUrl();

            if (linkedinUrl == null || linkedinUrl.isBlank()) {
                continue;
            }

            if (!processedProfiles.add(linkedinUrl)) {
                continue;
            }

            profilesProcessed++;

            Optional<RecruiterEmailResult> emailResult;

            try {
                emailResult = recruiterEmailService.findRecruiterEmail(linkedinUrl);

            } catch (HunterApiException exception) {

                failures.add(new RecruiterDiscoveryFailure(
                        linkedinUrl,
                        exception.getMessage(),
                        exception.getStatusCode()
                ));

                continue;
            }

            if (emailResult.isEmpty()) {
                continue;
            }

            emailsFound++;

            RecruiterResponse recruiter
                    = recruiterEmailService.saveDiscoveredRecruiter(
                            emailResult.get(),
                            user
                    );

            if (returnedRecruiterIds.add(recruiter.id())) {
                savedRecruiters.add(recruiter);
            }
        }

       return new BulkRecruiterDiscoveryResponse(
        totalProfilesFound,
        profilesProcessed,
        emailsFound,
        savedRecruiters.size(),
        failures.size(),
        savedRecruiters,
        failures
);
    }
}
