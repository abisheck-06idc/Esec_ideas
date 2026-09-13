package com.college.incubation.config;

import com.college.incubation.entity.Idea;
import com.college.incubation.entity.Role;
import com.college.incubation.entity.User;
import com.college.incubation.repository.IdeaRepository;
import com.college.incubation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final IdeaRepository ideaRepository;

    @Override
    public void run(String... args) {
        createUser("admin1@college.edu", "9000000001", "Admin", Role.ADMIN, "General");
        createUser("admin2@college.edu", "9000000002", "Administrator", Role.ADMIN, "General");

        for (Idea idea : ideaRepository.findAll()) {
            if (idea.getDepartment() == null || idea.getDepartment().isBlank()) {
                String department = idea.getUser() != null ? idea.getUser().getDepartment() : null;
                idea.setDepartment(department == null || department.isBlank() ? "General" : department);
                ideaRepository.save(idea);
            }
        }

        for (int i = 1; i <= 8; i++) {
            createUser("student" + i + "@college.edu", "900000000" + (i + 2),
                    "Student " + i, Role.STUDENT, "Computer Science & AI");
        }
    }

    private void createUser(String email, String phone, String name, Role role, String department) {
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            if (existing.getName() == null || existing.getName().isBlank()) existing.setName(name);
            if (existing.getDepartment() == null || existing.getDepartment().isBlank()) existing.setDepartment(department);
            if (existing.getRole() != role) existing.setRole(role);
            userRepository.save(existing);
        }, () -> userRepository.save(User.builder().email(email).phoneNumber(phone)
                .name(name).role(role).department(department).build()));
    }
}
