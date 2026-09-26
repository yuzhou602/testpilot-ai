package com.testpilot.agent;

import com.testpilot.common.enums.AgentTaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goal;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    @Builder.Default
    private AgentTaskStatus status = AgentTaskStatus.CREATED;

    @Column(columnDefinition = "TEXT")
    private String planJson;

    @Column(columnDefinition = "TEXT")
    private String resultSummary;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentStepIndex = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalSteps = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxSteps = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxRetry = 3;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentRetry = 0;

    @Column
    private Long totalTokens;

    @Column
    private Long totalLatencyMs;

    @Column(nullable = false)
    @Builder.Default
    private Boolean waitingForUser = false;

    @Column(columnDefinition = "TEXT")
    private String userConfirmationData;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime completedAt;
}
