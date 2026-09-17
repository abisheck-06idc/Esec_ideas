package com.college.incubation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateIdeaRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String description;

    @NotBlank
    @Size(max = 100)
    private String category;

    @NotBlank
    @Size(max = 100)
    private String department;

    @Size(max = 10000)
    private String problemStatement;

    @Size(max = 10000)
    private String solution;

    @Size(max = 255)
    private String attachmentName;

    @Size(max = 150)
    private String attachmentType;

    @Size(max = 9000000)
    private String attachmentData;
}
