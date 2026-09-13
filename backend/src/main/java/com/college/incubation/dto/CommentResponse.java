package com.college.incubation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {

    private UUID id;

    private UUID ideaId;

    private UUID adminId;

    private String adminEmail;

    private String commentText;

    private LocalDateTime createdAt;
}