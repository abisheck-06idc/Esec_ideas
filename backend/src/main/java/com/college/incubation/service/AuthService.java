package com.college.incubation.service;

import com.college.incubation.dto.LoginRequest;
import com.college.incubation.dto.LoginResponse;
import com.college.incubation.entity.User;
import com.college.incubation.repository.UserRepository;
import com.college.incubation.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final JwtUtils jwtUtils;

    public LoginResponse login(
            LoginRequest request
    ) {

        User user =
                userRepository
                        .findByEmailAndPhoneNumber(
                                request.getEmail(),
                                request.getPhoneNumber()
                        )
                        .orElseThrow(
                                () ->
                                        new BadCredentialsException(
                                                "Invalid email or phone number"
                                        )
                        );

        String token =
                jwtUtils.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName() == null || user.getName().isBlank() ? user.getEmail() : user.getName())
                .department(user.getDepartment())
                .build();
    }
}