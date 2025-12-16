package com.campuslfp.dto;

import com.campuslfp.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private Role role;
    private boolean verified;
    private boolean approved;
}
