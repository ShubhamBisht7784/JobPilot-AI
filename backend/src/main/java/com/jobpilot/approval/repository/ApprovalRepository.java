package com.jobpilot.approval.repository;

import com.jobpilot.approval.entity.Approval;
import com.jobpilot.approval.entity.ApprovalStatus;
import com.jobpilot.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

      Optional<Approval> findByIdAndUser(Long id, User user);

    Page<Approval> findByUserOrderByCreatedAtDesc(
            User user,
            Pageable pageable
    );

    Page<Approval> findByUserAndStatusOrderByCreatedAtDesc(
            User user,
            ApprovalStatus status,
            Pageable pageable
    );
}
