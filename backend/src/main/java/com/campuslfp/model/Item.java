package com.campuslfp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType type;

    private String category;

    private String tags; // comma-separated tags or keywords

    private String location;
    private String color;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    private Instant dateReported;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    // Whether this item has been matched to another (settable by admins)
    private Boolean matched = false;

    // Whether this item has been flagged for moderator review (can be set by any user)
    private Boolean flagged = false;

    @ManyToOne
    @JoinColumn(name = "posted_by_id")
    private User postedBy;
}
