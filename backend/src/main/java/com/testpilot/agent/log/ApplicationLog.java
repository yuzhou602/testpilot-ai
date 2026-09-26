package com.testpilot.agent.log;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 10)
    private String level;

    @Column(length = 50)
    private String logger;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(length = 50)
    private String exceptionClass;

    @Column(columnDefinition = "TEXT")
    private String extraData;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
