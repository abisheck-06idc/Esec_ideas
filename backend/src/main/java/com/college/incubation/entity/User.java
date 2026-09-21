package com.college.incubation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String name;

    @Column(name = "roll_no", nullable = true)
    private String rollNo;

    @Column(name = "student_year", nullable = true)
    private String year;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "profile_photo", columnDefinition = "TEXT", nullable = true)
    private String profilePhoto;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = true)
    @Builder.Default
    private String department = "General";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (department == null || department.isBlank()) {
            department = "General";
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (department == null || department.isBlank()) {
            department = "General";
        }
    }
}
