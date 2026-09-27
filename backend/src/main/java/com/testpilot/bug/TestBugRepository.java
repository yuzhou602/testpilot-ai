package com.testpilot.bug;

import com.testpilot.common.enums.BugSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestBugRepository extends JpaRepository<TestBug, Long> {
    List<TestBug> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    long countByProjectId(Long projectId);
    long countByProjectIdAndSeverity(Long projectId, BugSeverity severity);
}
