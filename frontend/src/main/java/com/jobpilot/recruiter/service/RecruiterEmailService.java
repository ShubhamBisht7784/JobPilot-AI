package com.jobpilot.recruiter.service;

import com.jobpilot.recruiter.dto.RecruiterEmailResult;
import com.jobpilot.recruiter.dto.RecruiterResponse;
import com.jobpilot.recruiter.entity.Recruiter;
import com.jobpilot.recruiter.integration.HunterApiClient;
import com.jobpilot.recruiter.repository.RecruiterRepository;
import com.jobpilot.auth.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecruiterEmailService {

    private final HunterApiClient hunterApiClient;
    private final RecruiterRepository recruiterRepository;

    public Optional<RecruiterEmailResult> findRecruiterEmail(String linkedinUrl) {

    JsonNode response = hunterApiClient.findEmailByLinkedIn(linkedinUrl);

    if (response == null) {
        return Optional.empty();
    }

    JsonNode data = response.path("data");

    if (data.isMissingNode() || data.isNull()) {
        return Optional.empty();
    }

    String email = data.path("email").asText("");

    if (email.isBlank()) {
        return Optional.empty();
    }

    String firstName = data.path("first_name").asText(null);
    String lastName = data.path("last_name").asText(null);
    String company = data.path("company").asText(null);
    String position = data.path("position").asText(null);
    String profileUrl = data.path("linkedin_url").asText("");

    if (profileUrl.isBlank()) {
        profileUrl = linkedinUrl;
    }

    Integer score = data.path("score").isNumber()
            ? data.path("score").asInt()
            : null;

    JsonNode verification = data.path("verification");

    String verificationStatus =
            verification.path("status").asText(null);

    String provenance = data.path("provenance").asText(null);

    return Optional.of(new RecruiterEmailResult(
            email,
            firstName,
            lastName,
            company,
            position,
            profileUrl,
            score,
            verificationStatus,
            provenance
    ));
}
    private String getText(JsonNode node, String fieldName) {

        JsonNode field = node.path(fieldName);

        if (field.isMissingNode() || field.isNull()) {
            return null;
        }

        return field.asText();
    }

    public RecruiterResponse saveDiscoveredRecruiter(
            RecruiterEmailResult result,
            User user) {

        Optional<Recruiter> existingRecruiter =
        recruiterRepository.findByEmailIgnoreCaseAndCompanyNameAndUser(
                result.email().trim(),
                result.company(),
                user
        );

        if (existingRecruiter.isPresent()) {

            Recruiter recruiter = existingRecruiter.get();

            // Fill missing profile information only.
            if ((recruiter.getName() == null
                    || recruiter.getName().isBlank()
                    || recruiter.getName().equals("Unknown"))
                    && result.firstName() != null) {

                String fullName = result.firstName();

                if (result.lastName() != null
                        && !result.lastName().isBlank()) {
                    fullName += " " + result.lastName();
                }

                recruiter.setName(fullName);
            }

            if ((recruiter.getDesignation() == null
                    || recruiter.getDesignation().isBlank())
                    && result.position() != null) {

                recruiter.setDesignation(result.position());
            }

            if ((recruiter.getLinkedinUrl() == null
                    || recruiter.getLinkedinUrl().isBlank())
                    && result.linkedinUrl() != null) {

                recruiter.setLinkedinUrl(result.linkedinUrl());
            }

            // Refresh Hunter verification metadata when available.
            if (result.score() != null) {
                recruiter.setEmailVerificationScore(result.score());
            }

            if (result.verificationStatus() != null) {
                recruiter.setEmailVerificationStatus(
                        result.verificationStatus()
                );
            }

            if (result.provenance() != null) {
                recruiter.setEmailProvenance(result.provenance());
            }

            return convertToResponse(recruiterRepository.save(recruiter));
        }

        Recruiter recruiter = new Recruiter();

        String fullName = result.firstName() != null
                ? result.firstName()
                : "Unknown";

        if (result.lastName() != null
                && !result.lastName().isBlank()) {
            fullName += " " + result.lastName();
        }

        recruiter.setName(fullName);
        recruiter.setEmail(result.email().trim().toLowerCase(Locale.ROOT));
        recruiter.setDesignation(result.position());
        recruiter.setCompanyName(result.company());
        recruiter.setLinkedinUrl(result.linkedinUrl());

        recruiter.setEmailVerificationScore(result.score());
        recruiter.setEmailVerificationStatus(result.verificationStatus());
        recruiter.setEmailProvenance(result.provenance());

        recruiter.setUser(user);

        return convertToResponse(recruiterRepository.save(recruiter));
    }

    private RecruiterResponse convertToResponse(Recruiter recruiter) {

        return new RecruiterResponse(
                recruiter.getId(),
                recruiter.getName(),
                recruiter.getEmail(),
                recruiter.getDesignation(),
                recruiter.getCompanyName(),
                recruiter.getLinkedinUrl(),
                recruiter.getNotes(),
                recruiter.getCreatedAt(),
                recruiter.getEmailVerificationScore(),
                recruiter.getEmailVerificationStatus(),
                recruiter.getEmailProvenance()
        );
    }
}
