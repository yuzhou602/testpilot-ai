package com.testpilot.bug;

import com.testpilot.common.enums.BugSeverity;
import com.testpilot.common.enums.BugStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_bugs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestBug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long executionId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String bugCode = "";

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private BugSeverity severity = BugSeverity.MAJOR;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private BugStatus status = BugStatus.OPEN;

    @Column(length = 100)
    private String module;

    @Column(columnDefinition = "TEXT")
    private String stepsToReproduce;

    @Column(columnDefinition = "TEXT")
    private String expectedResult;

    @Column(columnDefinition = "TEXT")
    private String actualResult;

    @Column(columnDefinition = "TEXT")
    private String screenshot;

    @Column(columnDefinition = "TEXT")
    private String httpRequest;

    @Column(columnDefinition = "TEXT")
    private String httpResponse;

    @Column(columnDefinition = "TEXT")
    private String applicationLog;

    @Column(columnDefinition = "TEXT")
    private String rootCauseHypothesis;

    @Column
    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String suspectedModule;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @Column(columnDefinition = "TEXT")
    private String suggestedFix;

    @Column
    @Builder.Default
    private Integer reproductionRate = 0;

    @Column
    @Builder.Default
    private Integer reproductionAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aiGenerated = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean confirmed = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
