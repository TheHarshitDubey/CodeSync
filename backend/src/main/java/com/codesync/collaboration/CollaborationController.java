package com.codesync.collaboration;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import static com.codesync.collaboration.CollabDtos.*;

@Controller
@RequiredArgsConstructor
public class CollaborationController {

    private final DocumentStore documentStore;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Client sends the full updated document content on every change.
     * We apply last-write-wins (see DocumentStore) and broadcast the
     * resulting version to everyone in the room, including the sender,
     * so all clients converge on the same state + version number.
     */
    @MessageMapping("/rooms/{roomCode}/edit")
    public void handleEdit(@DestinationVariable String roomCode,
                            EditMessage message,
                            SimpMessageHeaderAccessor headerAccessor) {
        long newVersion = documentStore.updateDocument(roomCode, message.content());

        BroadcastEdit broadcast = new BroadcastEdit(message.username(), message.content(), newVersion);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode, broadcast);
    }

    @MessageMapping("/rooms/{roomCode}/join")
    public void handleJoin(@DestinationVariable String roomCode,
                            PresenceMessage message,
                            SimpMessageHeaderAccessor headerAccessor) {
        documentStore.addParticipant(roomCode, message.username());

        // remember which room/user this WS session belongs to, for cleanup on disconnect
        headerAccessor.getSessionAttributes().put("roomCode", roomCode);
        headerAccessor.getSessionAttributes().put("username", message.username());

        PresenceUpdate update = new PresenceUpdate(
                documentStore.getParticipants(roomCode), "JOIN", message.username());
        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/presence", update);
    }

    @MessageMapping("/rooms/{roomCode}/leave")
    public void handleLeave(@DestinationVariable String roomCode, PresenceMessage message) {
        documentStore.removeParticipant(roomCode, message.username());

        PresenceUpdate update = new PresenceUpdate(
                documentStore.getParticipants(roomCode), "LEAVE", message.username());
        messagingTemplate.convertAndSend("/topic/rooms/" + roomCode + "/presence", update);
    }
}
