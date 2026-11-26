
package com.jobpilot.recruiter.service;

import com.jobpilot.auth.entity.User;
import com.jobpilot.recruiter.dto.RecruiterRequest;
import com.jobpilot.recruiter.dto.RecruiterResponse;
import com.jobpilot.recruiter.entity.Recruiter;
import com.jobpilot.recruiter.repository.RecruiterRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final RecruiterRepository recruiterRepository;

    @Transactional
    public RecruiterResponse createRecruiter(
            RecruiterRequest request,
            User user) {

        boolean duplicateExists =
                recruiterRepository
                        .existsByEmailIgnoreCaseAndCompanyNameIgnoreCaseAndUser(
                                request.email().trim(),
                                request.companyName().trim(),
                                user
                        );

        if (duplicateExists) {
            throw new IllegalArgumentException(
                    "Recruiter with this email and company already exists."
            );
        }

        Recruiter recruiter = new Recruiter();

        recruiter.setUser(user);
        recruiter.setName(request.name());
        recruiter.setEmail(
                request.email().trim().toLowerCase(Locale.ROOT)
        );
        recruiter.setDesignation(request.designation());
        recruiter.setCompanyName(request.companyName());
        recruiter.setLinkedinUrl(request.linkedinUrl());
        recruiter.setNotes(request.notes());

        Recruiter savedRecruiter = recruiterRepository.save(recruiter);

        return convertToResponse(savedRecruiter);
    }

    @Transactional(readOnly = true)
    public Page<RecruiterResponse> getUserRecruiters(
            User user,
            Pageable pageable) {

        return recruiterRepository.findByUser(user, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public RecruiterResponse getRecruiterById(
            Long recruiterId,
            User user) {

        Recruiter recruiter = findRecruiter(recruiterId, user);

        return convertToResponse(recruiter);
    }

    @Transactional
    public RecruiterResponse updateRecruiter(
            Long recruiterId,
            RecruiterRequest request,
            User user) {

        Recruiter recruiter = findRecruiter(recruiterId, user);

        boolean duplicateExists =
                recruiterRepository
                        .existsByEmailIgnoreCaseAndCompanyNameIgnoreCaseAndUserAndIdNot(
                                request.email().trim(),
                                request.companyName().trim(),
                                user,
                                recruiterId
                        );

        if (duplicateExists) {
            throw new IllegalArgumentException(
                    "Another recruiter with this email and company already exists."
            );
        }

        recruiter.setName(request.name());
        recruiter.setEmail(
                request.email().trim().toLowerCase(Locale.ROOT)
        );
        recruiter.setDesignation(request.designation());
        recruiter.setCompanyName(request.companyName());
        recruiter.setLinkedinUrl(request.linkedinUrl());
        recruiter.setNotes(request.notes());

        Recruiter updatedRecruiter = recruiterRepository.save(recruiter);

        return convertToResponse(updatedRecruiter);
    }

    @Transactional
    public void deleteRecruiter(Long recruiterId, User user) {

        Recruiter recruiter = findRecruiter(recruiterId, user);

        recruiterRepository.delete(recruiter);
    }

    private Recruiter findRecruiter(Long recruiterId, User user) {

        return recruiterRepository.findByIdAndUser(recruiterId, user)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recruiter not found with id: " + recruiterId
                ));
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
