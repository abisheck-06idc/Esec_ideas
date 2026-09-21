package com.college.incubation.dto;

import com.college.incubation.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(max = 100)
    private String name;

    @NotBlank @Size(max = 100)
    private String rollNo;

    @NotBlank @Size(max = 50)
    private String year;

    @NotBlank @Size(max = 100)
    private String department;

    @NotBlank @Email @Size(max = 255)
    private String email;

    @NotBlank @Size(max = 100)
    private String phoneNumber;

    private Role role;
}
