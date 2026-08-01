package com.codesync.collaboration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static com.codesync.collaboration.CollabDtos.PresenceUpdate;

/**
 * Handles the case where a user just closes the tab instead of sending
 * an explicit "leave" message — otherwise they'd stay listed as active
 * in the room forever (until the Redis TTL expires).
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final DocumentStore documentStore;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        if (accessor.getSessionAttributes() == null) return;

        String roomCode = (String) accessor.getSessionAttributes().get("roomCode");
        String username = (String) accessor.getSessionAttributes().get("username");

        if (roomCode != null && username != null) {
            documentStore.removeParticipant(roomCode, username);
            PresenceUpdate update = new PresenceUpdate(
                    documentStore.getParticipants(roomCode), "LEAVE", username);
            messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/presence", update);
        }
    }
}
