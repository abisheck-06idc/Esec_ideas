package com.college.incubation.repository;

import com.college.incubation.entity.Idea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdeaRepository extends JpaRepository<Idea, UUID> {

    List<Idea> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Idea> findByIdAndUserId(UUID ideaId, UUID userId);

    List<Idea> findAllByDepartmentOrderByCreatedAtDesc(String department);
}
