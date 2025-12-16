package com.campuslfp.service;

import com.campuslfp.dto.AuthResponse;
import com.campuslfp.dto.LoginRequest;
import com.campuslfp.dto.RegisterRequest;
import com.campuslfp.model.Role;
import com.campuslfp.model.User;
import com.campuslfp.repository.UserRepository;
import com.campuslfp.security.CustomUserDetails;
import com.campuslfp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // Change this to your campus domain
    private static final String CAMPUS_DOMAIN = "@college.edu";

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        boolean verified = request.getEmail().toLowerCase().endsWith(CAMPUS_DOMAIN);

        // Determine approval: educational emails auto-approved, others require admin approval
        boolean approved = verified;

        // Basic server-side password strength validation (must contain digit, special char, upper and lower case)
        String pwd = request.getPassword();
        if (!isPasswordStrong(pwd)) {
            throw new RuntimeException("Password must include uppercase, lowercase, number and a special character and be at least 6 characters long");
        }

        if (!request.isTermsAccepted()) {
            throw new RuntimeException("You must accept the terms and policy to register");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_STUDENT)
                .verified(verified)
                .approved(approved)
        .ignored(false)
                .department(request.getDepartment())
                .contactNo(request.getContactNo())
                .termsAccepted(request.isTermsAccepted())
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        // Return response with approval status. 
        // If approved, user can now login via /api/auth/login.
        // If pending, user needs admin approval before they can login.
        return new AuthResponse(null, user.getName(), user.getEmail(), user.getRole(), user.isVerified(), user.isApproved());
    }

    public AuthResponse login(LoginRequest request) {
        // Pre-check user status to provide clearer errors
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            if (u.isBlocked()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are blocked");
            }
            if (!u.isApproved()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pending admin approval");
            }
        });

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        String token = jwtTokenProvider.generateToken(authentication);

        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole(), user.isVerified(), user.isApproved());
    }

    private boolean isPasswordStrong(String pwd) {
        if (pwd == null || pwd.length() < 6) {
            return false;
        }
        boolean hasUpper = pwd.matches(".*[A-Z].*");
        boolean hasLower = pwd.matches(".*[a-z].*");
        boolean hasDigit = pwd.matches(".*\\d.*");
        boolean hasSpecial = pwd.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
