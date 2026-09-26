package com.testpilot.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestProjectRepository extends JpaRepository<TestProject, Long> {
    List<TestProject> findByUserIdOrderByCreatedAtDesc(Long userId);
}
