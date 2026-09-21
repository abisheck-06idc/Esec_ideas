package com.college.incubation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailResponse {
    private String id;
    private String name;
    private String rollNo;
    private String year;
    private String department;
    private String email;
    private String phoneNumber;
    private String role;
}