package com.testpilot.agent.evaluation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_evaluations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private Long projectId;

    @Column
    private Double requirementUnderstandingScore;

    @Column
    private Double testCaseQualityScore;

    @Column
    private Double coverageScore;

    @Column
    private Double bugDetectionScore;

    @Column
    private Double rootCauseAccuracyScore;

    @Column
    private Double toolSelectionScore;

    @Column
    private Double completionRateScore;

    @Column(columnDefinition = "TEXT")
    private String evaluationDetails;

    @Column(length = 100)
    private String promptVersion;

    @Column(length = 100)
    private String modelUsed;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime evaluatedAt;
}
