package com.codesync.collaboration;

import java.util.Set;

public class CollabDtos {

    /** Sent by a client whenever the editor content changes. */
    public record EditMessage(
            String username,
            String content,
            long clientVersion
    ) {}

    /** Broadcast to all clients in the room after an edit is applied. */
    public record BroadcastEdit(
            String username,
            String content,
            long version
    ) {}

    /** Sent by a client on join/leave. */
    public record PresenceMessage(
            String username,
            String type // "JOIN" or "LEAVE"
    ) {}

    /** Broadcast with the current participant list. */
    public record PresenceUpdate(
            Set<String> participants,
            String lastEvent,
            String username
    ) {}
}
