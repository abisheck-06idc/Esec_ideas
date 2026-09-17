package com.college.incubation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ideas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Idea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "problem_statement", columnDefinition = "TEXT", nullable = true)
    private String problemStatement;

    @Column(name = "solution", columnDefinition = "TEXT", nullable = true)
    private String solution;

    @Column(name = "attachment_name", nullable = true)
    private String attachmentName;

    @Column(name = "attachment_type", nullable = true)
    private String attachmentType;

    @Column(name = "attachment_data", columnDefinition = "TEXT", nullable = true)
    private String attachmentData;

    @Column(nullable = true)
    @Builder.Default
    private String department = "General";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (department == null || department.isBlank()) {
            department = user != null && user.getDepartment() != null
                    ? user.getDepartment()
                    : "General";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
