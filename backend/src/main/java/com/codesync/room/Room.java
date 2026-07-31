package com.codesync.room;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false, length = 8)
    private String roomCode; // short shareable code, e.g. "A1B2C3"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String createdBy; // username of owner

    @Builder.Default
    private String language = "java";

    @Builder.Default
    private Instant createdAt = Instant.now();
}
