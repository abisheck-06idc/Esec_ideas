package com.college.incubation.controller;

import com.college.incubation.dto.LoginResponse;
import com.college.incubation.dto.UpdateProfileRequest;
import com.college.incubation.entity.User;
import com.college.incubation.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;

    @GetMapping("/me")
    public LoginResponse me(Authentication authentication) {
        return response(getUser(authentication));
    }

    @PutMapping("/me")
    public LoginResponse update(@Valid @RequestBody UpdateProfileRequest request,
                                Authentication authentication) {
        User user = getUser(authentication);
        if (request.getName() != null) user.setName(request.getName().trim());
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) user.setPhoneNumber(request.getPhoneNumber().trim());
        if (request.getDepartment() != null && !request.getDepartment().isBlank()) user.setDepartment(request.getDepartment().trim());
        if (request.getProfilePhoto() != null) user.setProfilePhoto(request.getProfilePhoto());
        return response(userRepository.save(user));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private LoginResponse response(User user) {
        return LoginResponse.builder()
                .id(user.getId()).email(user.getEmail()).role(user.getRole().name())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName() == null || user.getName().isBlank() ? user.getEmail() : user.getName())
                .rollNo(user.getRollNo())
                .year(user.getYear())
                .department(user.getDepartment())
                .profilePhoto(user.getProfilePhoto()).build();
    }
}
