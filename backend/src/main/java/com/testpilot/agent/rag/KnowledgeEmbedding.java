package com.testpilot.agent.rag;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_embeddings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(length = 200)
    private String source;

    @Column
    @Builder.Default
    private Double relevanceScore = 0.0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
