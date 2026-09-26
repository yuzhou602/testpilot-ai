package com.testpilot.requirement;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "requirement_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    private Requirement requirement;

    @Column(nullable = false, length = 100)
    private String ruleCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleDescription;

    @Column(nullable = false)
    @Builder.Default
    private Boolean covered = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer coverageCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
