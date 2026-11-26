
package com.jobpilot.resume.repository;

import com.jobpilot.resume.entity.Resume;
import com.jobpilot.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUser(User user);

    Optional<Resume> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);

    Optional<Resume> findFirstByUserOrderByUploadedAtDesc(User user);
}
