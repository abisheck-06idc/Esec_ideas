package com.college.incubation.controller;

import com.college.incubation.dto.CommentResponse;
import com.college.incubation.dto.CreateIdeaRequest;
import com.college.incubation.dto.IdeaResponse;
import com.college.incubation.dto.UpdateIdeaRequest;
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
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;
    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public IdeaResponse createIdea(@Valid @RequestBody CreateIdeaRequest request,
                                   Authentication authentication) {
        return ideaService.createIdea(request, authentication.getName());
    }

    @GetMapping("/my-ideas")
    @PreAuthorize("hasRole('STUDENT')")
    public List<IdeaResponse> getMyIdeas(Authentication authentication) {
        return ideaService.getMyIdeas(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public IdeaResponse getIdea(@PathVariable UUID id, Authentication authentication) {
        return ideaService.getMyIdea(id, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public IdeaResponse updateIdea(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateIdeaRequest request,
                                   Authentication authentication) {
        return ideaService.updateMyIdea(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public void deleteIdea(@PathVariable UUID id, Authentication authentication) {
        ideaService.deleteMyIdea(id, authentication.getName());
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public List<CommentResponse> getComments(@PathVariable UUID id,
                                             Authentication authentication) {
        return commentService.getCommentsForAuthorizedUser(id, authentication);
    }
}
