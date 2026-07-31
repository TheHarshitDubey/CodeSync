package com.codesync.room;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public class RoomDtos {

    public record CreateRoomRequest(
            @NotBlank String name,
            String language // optional, defaults to "java"
    ) {}

    public record JoinRoomRequest(
            @NotBlank String roomCode
    ) {}

    public record RoomResponse(
            UUID id,
            String roomCode,
            String name,
            String createdBy,
            String language,
            Instant createdAt
    ) {
        public static RoomResponse from(Room room) {
            return new RoomResponse(
                    room.getId(), room.getRoomCode(), room.getName(),
                    room.getCreatedBy(), room.getLanguage(), room.getCreatedAt()
            );
        }
    }
}
