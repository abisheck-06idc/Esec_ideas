package com.college.incubation.dto;

import com.college.incubation.entity.IdeaStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull
    private IdeaStatus status;
}