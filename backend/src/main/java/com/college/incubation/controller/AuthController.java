package com.college.incubation.controller;

import com.college.incubation.dto.LoginRequest;
import com.college.incubation.dto.RegisterRequest;
import com.college.incubation.dto.LoginResponse;
import com.college.incubation.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        return authService.login(request);
    }


    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
