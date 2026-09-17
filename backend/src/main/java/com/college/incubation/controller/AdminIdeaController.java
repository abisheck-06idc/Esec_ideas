package com.college.incubation.controller;

import com.college.incubation.dto.CommentResponse;
import com.college.incubation.dto.CreateCommentRequest;
import com.college.incubation.dto.IdeaResponse;
import com.college.incubation.dto.UpdateStatusRequest;
import com.college.incubation.service.CommentService;
import com.college.incubation.service.IdeaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/ideas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminIdeaController {

    private final IdeaService ideaService;

    private final CommentService commentService;

    @GetMapping
    public List<IdeaResponse> getAllIdeas() {

        return ideaService.getAllIdeas();
    }

    @GetMapping("/{id}")
    public IdeaResponse getIdea(@PathVariable UUID id) {
        return ideaService.getAllIdeas().stream()
                .filter(idea -> idea.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Idea not found"));
    }

    @PatchMapping("/{id}/status")
    public IdeaResponse updateStatus(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            UpdateStatusRequest request
    ) {

        return ideaService.updateStatus(
                id,
                request.getStatus()
        );
    }

    @PostMapping("/{id}/comments")
    public CommentResponse addComment(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            CreateCommentRequest request,

            Authentication authentication
    ) {

        return commentService.addComment(
                id,
                request,
                authentication.getName()
        );
    }
}