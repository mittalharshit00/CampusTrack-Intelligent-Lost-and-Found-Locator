package com.campuslfp.service;

import com.campuslfp.dto.response.AuthResponse;
import com.campuslfp.dto.request.LoginRequest;
import com.campuslfp.dto.request.RegisterRequest;
import com.campuslfp.exception.BadRequestException;
import com.campuslfp.exception.ConflictException;
import com.campuslfp.exception.ResourceNotFoundException;
import com.campuslfp.mapper.RegistrationMapper;
import com.campuslfp.mapper.UserMapper;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final RegistrationMapper registrationMapper;

    private static final String CAMPUS_EMAIL_SUFFIX = "@college.edu";

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        boolean verified = request.getEmail().toLowerCase().endsWith(CAMPUS_EMAIL_SUFFIX);
        boolean approved = verified;

        String rawPassword = request.getPassword();
        if (!isPasswordStrong(rawPassword)) {
            throw new BadRequestException(
                    "Password must include uppercase, lowercase, number and a special character and be at least 6 characters long");
        }

        if (!Boolean.TRUE.equals(request.getTermsAccepted())) {
            throw new BadRequestException("You must accept the terms and policy to register");
        }

        User user = registrationMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(verified);
        user.setApproved(approved);
        user.setCreatedAt(java.time.Instant.now());

        userRepository.save(user);

        AuthResponse response = userMapper.toAuthResponse(user);
        response.setToken(null);
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isBlocked()) {
            throw new BadRequestException("You are blocked");
        }
        if (!user.isApproved()) {
            throw new BadRequestException("Pending admin approval");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User authenticatedUser = userDetails.getUser();
        String token = jwtTokenProvider.generateToken(authentication);

        AuthResponse response = userMapper.toAuthResponse(authenticatedUser);
        response.setToken(token);
        return response;
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
