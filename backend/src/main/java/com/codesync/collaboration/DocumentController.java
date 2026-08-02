package com.codesync.collaboration;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentStore documentStore;

    /**
     * A user opening a room fetches the current document snapshot over REST
     * first, then subscribes to the WebSocket topic for live updates from
     * that point on. Avoids needing the WS connection to carry the full
     * initial state.
     */
    @GetMapping("/{roomCode}/document")
    public ResponseEntity<DocumentSnapshot> getDocument(@PathVariable String roomCode) {
        String content = documentStore.getDocument(roomCode);
        long version = documentStore.getVersion(roomCode);
        return ResponseEntity.ok(new DocumentSnapshot(content, version));
    }

    public record DocumentSnapshot(String content, long version) {}
}
