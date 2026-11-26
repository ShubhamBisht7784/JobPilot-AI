
package com.jobpilot.email.repository;

import com.jobpilot.email.entity.GmailConnection;
import com.jobpilot.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GmailConnectionRepository
        extends JpaRepository<GmailConnection, Long> {

    Optional<GmailConnection> findByUser(User user);

    Optional<GmailConnection> findByGmailAddress(String gmailAddress);

    Optional<GmailConnection> findByUser_Email(String email);

    boolean existsByUser(User user);

    void deleteByUser(User user);
}
