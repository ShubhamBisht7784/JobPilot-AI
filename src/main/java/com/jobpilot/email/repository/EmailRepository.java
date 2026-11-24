
package com.jobpilot.email.repository;

import com.jobpilot.auth.entity.User;
import com.jobpilot.email.entity.Email;
import com.jobpilot.email.entity.EmailStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    // Find an email belonging to a specific user
    Optional<Email> findByIdAndUser(Long id, User user);

    // Get all emails of a user with pagination
    Page<Email> findByUserOrderByCreatedAtDesc(
            User user,
            Pageable pageable
    );

    // Filter emails by status for a specific user
    Page<Email> findByUserAndStatusOrderByCreatedAtDesc(
            User user,
            EmailStatus status,
            Pageable pageable
    );

    // Check whether an email belongs to the given user
    boolean existsByIdAndUser(Long id, User user);

    Optional<Email> findByApprovalId(Long approvalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Email e where e.approvalId = :approvalId")
    Optional<Email> findByApprovalIdForUpdate(@Param("approvalId") Long approvalId);
}
