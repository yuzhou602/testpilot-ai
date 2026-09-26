package com.testpilot.report;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long agentTaskId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column
    private Integer totalTests;

    @Column
    private Integer passedTests;

    @Column
    private Integer failedTests;

    @Column
    private Integer skippedTests;

    @Column
    private Double passRate;

    @Column(columnDefinition = "TEXT")
    private String requirementCoverage;

    @Column(columnDefinition = "TEXT")
    private String riskAssessment;

    @Column(columnDefinition = "TEXT")
    private String bugsSummary;

    @Column(columnDefinition = "TEXT")
    private String aiRecommendations;

    @Column
    private Long totalExecutionTimeMs;

    @Column
    private Long totalTokensUsed;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime generatedAt;
}
