package com.college.incubation.repository;

import com.college.incubation.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository
        extends JpaRepository<Comment, UUID> {

    List<Comment>
    findAllByIdeaIdOrderByCreatedAtAsc(
            UUID ideaId
    );
}