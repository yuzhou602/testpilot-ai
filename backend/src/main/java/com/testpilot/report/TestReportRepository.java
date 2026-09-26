package com.testpilot.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestReportRepository extends JpaRepository<TestReport, Long> {
    List<TestReport> findByProjectIdOrderByGeneratedAtDesc(Long projectId);
}
