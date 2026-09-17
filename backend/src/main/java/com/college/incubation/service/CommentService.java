package com.college.incubation.service;

import com.college.incubation.dto.CommentResponse;
import com.college.incubation.dto.CreateCommentRequest;
import com.college.incubation.entity.Comment;
import com.college.incubation.entity.Idea;
import com.college.incubation.entity.Role;
import com.college.incubation.entity.User;
import com.college.incubation.repository.CommentRepository;
import com.college.incubation.repository.IdeaRepository;
import com.college.incubation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final IdeaRepository ideaRepository;

    private final UserRepository userRepository;

    public CommentResponse addComment(
            UUID ideaId,
            CreateCommentRequest request,
            String reviewerEmail
    ) {

        Idea idea =
                ideaRepository
                        .findById(ideaId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Idea not found"
                                        )
                        );

        User reviewer =
                userRepository
                        .findByEmail(reviewerEmail)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Reviewer not found"
                                        )
                        );

        Comment comment =
                Comment.builder()
                        .idea(idea)
                        .admin(reviewer)
                        .commentText(
                                request.getCommentText()
                        )
                        .build();

        return toResponse(
                commentRepository.save(comment)
        );
    }

    public List<CommentResponse>
    getCommentsForAuthorizedUser(
            UUID ideaId,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        Idea idea =
                ideaRepository
                        .findById(ideaId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Idea not found"
                                        )
                        );

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = idea.getUser().getId().equals(user.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You cannot view these comments");
        }

        return commentRepository
                .findAllByIdeaIdOrderByCreatedAtAsc(
                        ideaId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(
            Comment comment
    ) {

        return CommentResponse.builder()

                .id(comment.getId())

                .ideaId(
                        comment.getIdea().getId()
                )

                .adminId(
                        comment.getAdmin().getId()
                )

                .adminEmail(
                        comment.getAdmin().getEmail()
                )

                .commentText(
                        comment.getCommentText()
                )

                .createdAt(
                        comment.getCreatedAt()
                )

                .build();
    }
}