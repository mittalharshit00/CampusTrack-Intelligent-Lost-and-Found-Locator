package com.campuslfp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User userA;

    @ManyToOne
    private User userB;

    @ManyToOne
    private Item item; // The item for which this conversation is about

    private Instant createdAt;

    // Whether the recipient approved the conversation (true when either participant allowed)
    private boolean approved = false;

    // Blocking flags: if userA blocks userB, blockedByA=true (then userB cannot send messages to A)
    private boolean blockedByA = false;
    private boolean blockedByB = false;
}
