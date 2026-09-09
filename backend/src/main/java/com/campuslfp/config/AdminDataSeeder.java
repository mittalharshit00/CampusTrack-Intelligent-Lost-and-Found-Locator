package com.campuslfp.config;

import com.campuslfp.enums.Role;
import com.campuslfp.model.User;
import com.campuslfp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AdminDataSeeder implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@college.edu";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(user -> {
            if (user.getRole() != Role.ROLE_ADMIN) {
                user.setRole(Role.ROLE_ADMIN);
                user.setApproved(true);
                user.setVerified(true);
                user.setBlocked(false);
                user.setIgnored(false);
                userRepository.save(user);
            }
        }, () -> {
            User admin = User.builder()
                    .name("System Admin")
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(Role.ROLE_ADMIN)
                    .verified(true)
                    .approved(true)
                    .ignored(false)
                    .department("Administration")
                    .contactNo("+0000000000")
                    .termsAccepted(true)
                    .createdAt(Instant.now())
                    .build();

            userRepository.save(admin);
        });
    }
}
