package com.college.incubation.dto;

import com.college.incubation.entity.IdeaStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class IdeaResponse {

    private UUID id;
    private UUID userId;
    private String studentEmail;
    private String studentName;
    private String department;
    private String title;
    private String description;
    private String category;
    private String problemStatement;
    private String solution;
    private String attachmentName;
    private String attachmentType;
    private String attachmentData;
    private IdeaStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
