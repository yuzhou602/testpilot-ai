package com.testpilot.api;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(length = 200)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requestSchema;

    @Column(columnDefinition = "TEXT")
    private String responseSchema;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String security;

    @Column(length = 100)
    private String tag;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
