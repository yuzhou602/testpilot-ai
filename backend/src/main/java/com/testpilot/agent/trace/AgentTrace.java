package com.testpilot.agent.trace;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_traces")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private Integer stepIndex;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(length = 100)
    private String toolName;

    @Column(length = 50)
    private String status;

    @Column
    private Long latencyMs;

    @Column
    private Long tokensUsed;

    @Column(length = 100)
    private String model;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
