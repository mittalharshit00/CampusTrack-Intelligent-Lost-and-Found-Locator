package com.campuslfp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean verified;

    // Whether this account has been approved/activated by an admin. Educational emails may be auto-approved.
    private boolean approved;

    // Whether this account has been ignored by an admin (do not show in pending list)
    private Boolean ignored = false;

    // Whether this account is blocked by an admin (cannot login)
    private boolean blocked = false;

    // Additional profile fields
    private String department;
    private String contactNo;
    private boolean termsAccepted;

    private Instant createdAt;
}
