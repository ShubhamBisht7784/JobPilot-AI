package com.jobpilot.recruiter.repository;

import com.jobpilot.auth.entity.User;
import com.jobpilot.recruiter.entity.Recruiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {

    Page<Recruiter> findByUser(User user, Pageable pageable);

    Optional<Recruiter> findByIdAndUser(Long id, User user);

    boolean existsByEmailAndCompanyNameAndUser(
            String email,
            String companyName,
            User user
    );

    boolean existsByEmailAndCompanyNameAndUserAndIdNot(
            String email,
            String companyName,
            User user,
            Long id
    );

    Optional<Recruiter> findByEmailAndCompanyNameAndUser(
        String email,
        String companyName,
        User user
);

Optional<Recruiter> findByEmailIgnoreCaseAndCompanyNameAndUser(
        String email,
        String companyName,
        User user
);

boolean existsByEmailIgnoreCaseAndCompanyNameIgnoreCaseAndUser(
        String email,
        String companyName,
        User user
);

boolean existsByEmailIgnoreCaseAndCompanyNameIgnoreCaseAndUserAndIdNot(
        String email,
        String companyName,
        User user,
        Long id
);
}
