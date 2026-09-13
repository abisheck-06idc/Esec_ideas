package com.college.incubation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LoginResponse {

    private String token;
    private UUID id;
    private String email;
    private String role;
    private String phoneNumber;
    private String name;
    private String department;
    private String profilePhoto;
}
