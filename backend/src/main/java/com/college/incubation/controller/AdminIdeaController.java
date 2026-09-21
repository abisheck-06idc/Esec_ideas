package com.college.incubation.controller;

import com.college.incubation.dto.CommentResponse;
import com.college.incubation.dto.CreateCommentRequest;
import com.college.incubation.dto.IdeaResponse;
import com.college.incubation.dto.UpdateStatusRequest;
import com.college.incubation.dto.UserDetailResponse;
import com.college.incubation.entity.Role;
import com.college.incubation.entity.User;
import com.college.incubation.repository.UserRepository;
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

    private final UserRepository userRepository;

    @GetMapping
    public List<IdeaResponse> getAllIdeas() {

        return ideaService.getAllIdeas();
    }

    @GetMapping("/users")
    public List<UserDetailResponse> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(user -> UserDetailResponse.builder()
                        .id(String.valueOf(user.getId()))
                        .name(user.getName() == null || user.getName().isBlank() ? user.getEmail() : user.getName())
                        .rollNo(user.getRollNo())
                        .year(user.getYear())
                        .department(user.getDepartment())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole() == null ? Role.STUDENT.name() : user.getRole().name())
                        .build())
                .toList();
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