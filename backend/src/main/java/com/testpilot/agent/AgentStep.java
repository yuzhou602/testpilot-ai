package com.testpilot.agent;

import com.testpilot.common.enums.StepStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private Integer stepIndex;

    @Column(nullable = false, length = 100)
    private String stepName;

    @Column(length = 50)
    private String stepType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private StepStatus status = StepStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String toolName;

    @Column(columnDefinition = "TEXT")
    private String toolInput;

    @Column(columnDefinition = "TEXT")
    private String toolOutput;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private Long latencyMs;

    @Column
    private Long tokensUsed;

    @Column(length = 100)
    private String model;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startedAt;

    @UpdateTimestamp
    private LocalDateTime completedAt;
}
