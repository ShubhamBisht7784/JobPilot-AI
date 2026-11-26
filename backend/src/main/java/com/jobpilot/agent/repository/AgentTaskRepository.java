
package com.jobpilot.agent.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jobpilot.agent.entity.AgentTask;
import com.jobpilot.agent.enums.AgentTaskStatus;
import com.jobpilot.auth.entity.User;

public interface AgentTaskRepository extends JpaRepository<AgentTask, Long> {

    Page<AgentTask> findByUser(User user, Pageable pageable);

    Optional<AgentTask> findByIdAndUser(Long id, User user);

    Page<AgentTask> findByUserAndStatus(
            User user,
            AgentTaskStatus status,
            Pageable pageable
    );

    long countByUserAndStatus(User user, AgentTaskStatus status);

}
