package com.testpilot.execution;

import com.testpilot.common.enums.TestResultStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long testCaseId;

    @Column(nullable = false)
    private Long agentTaskId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private TestResultStatus status = TestResultStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String actualResult;

    @Column(columnDefinition = "TEXT")
    private String assertionResult;

    @Column
    private Long latencyMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column
    private Integer attempt;

    @Column(columnDefinition = "TEXT")
    private String httpRequest;

    @Column(columnDefinition = "TEXT")
    private String httpResponse;

    @Column(columnDefinition = "TEXT")
    private String screenshot;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime executedAt;
}
