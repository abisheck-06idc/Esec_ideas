package com.college.incubation.repository;

import com.college.incubation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(
            String email
    );

    Optional<User> findByEmailAndPhoneNumber(
            String email,
            String phoneNumber
    );
}