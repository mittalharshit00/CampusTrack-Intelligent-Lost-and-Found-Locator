package com.campuslfp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "flags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User reporter;

    // Can be item OR message
    private Long itemId;
    private Long messageId;

    private String reason;
    private String status;

    private Instant createdAt;
}
