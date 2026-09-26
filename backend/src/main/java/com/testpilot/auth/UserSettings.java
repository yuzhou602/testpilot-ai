package com.testpilot.auth;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 50)
    @Builder.Default
    private String theme = "dark";

    @Column(length = 50)
    @Builder.Default
    private String language = "zh-CN";

    @Column(length = 200)
    private String openaiApiKey;

    @Column(length = 200)
    private String openaiBaseUrl;

    @Column(length = 100)
    @Builder.Default
    private String openaiModel = "gpt-4o";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
