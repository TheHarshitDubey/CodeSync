package com.codesync.room;

import com.codesync.collaboration.DocumentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import static com.codesync.room.RoomDtos.*;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no O/0/I/1 to avoid confusion
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RoomRepository roomRepository;
    private final DocumentStore documentStore;

    public RoomResponse createRoom(CreateRoomRequest request, String username) {
        Room room = Room.builder()
                .roomCode(generateUniqueCode())
                .name(request.name())
                .createdBy(username)
                .language(request.language() != null ? request.language() : "java")
                .build();

        roomRepository.save(room);
        documentStore.initDocument(room.getRoomCode(), starterCode(room.getLanguage()));

        return RoomResponse.from(room);
    }

    public RoomResponse getRoomByCode(String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomCode));
        return RoomResponse.from(room);
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (roomRepository.findByRoomCode(code).isPresent());
        return code;
    }

    private String starterCode(String language) {
        return switch (language) {
            case "python" -> "print('Hello from CodeSync')\n";
            case "javascript" -> "console.log('Hello from CodeSync');\n";
            default -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from CodeSync\");\n    }\n}\n";
        };
    }
}
