package com.codesync.room;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.codesync.room.RoomDtos.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request,
                                                    @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(roomService.createRoom(request, principal.getUsername()));
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(roomService.getRoomByCode(roomCode));
    }
}
