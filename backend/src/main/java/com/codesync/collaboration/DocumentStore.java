package com.codesync.collaboration;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Ephemeral room state lives in Redis, not Postgres — mirrors the reference
 * project's design decision: durable identity (Room metadata) in Postgres,
 * hot/live document content + presence in Redis, so the write path for every
 * keystroke stays fast and we're not hammering the relational DB.
 */
@Component
@RequiredArgsConstructor
public class DocumentStore {

    private static final Duration DOC_TTL = Duration.ofHours(6);

    private final RedisTemplate<String, String> redisTemplate;

    private String docKey(String roomCode) {
        return "doc:" + roomCode;
    }

    private String presenceKey(String roomCode) {
        return "presence:" + roomCode;
    }

    private String versionKey(String roomCode) {
        return "version:" + roomCode;
    }

    public void initDocument(String roomCode, String starterContent) {
        redisTemplate.opsForValue().setIfAbsent(docKey(roomCode), starterContent, DOC_TTL);
        redisTemplate.opsForValue().setIfAbsent(versionKey(roomCode), "0", DOC_TTL);
    }

    public String getDocument(String roomCode) {
        String content = redisTemplate.opsForValue().get(docKey(roomCode));
        return content != null ? content : "";
    }

    public long getVersion(String roomCode) {
        String v = redisTemplate.opsForValue().get(versionKey(roomCode));
        return v != null ? Long.parseLong(v) : 0L;
    }

    /**
     * Last-write-wins with a monotonically increasing version counter.
     * This is the v1 conflict strategy (documented, deliberate trade-off —
     * see README "Next Steps" for the planned OT/CRDT upgrade path).
     */
    public long updateDocument(String roomCode, String fullContent) {
        redisTemplate.opsForValue().set(docKey(roomCode), fullContent, DOC_TTL);
        Long newVersion = redisTemplate.opsForValue().increment(versionKey(roomCode));
        redisTemplate.expire(versionKey(roomCode), DOC_TTL);
        return newVersion != null ? newVersion : 0L;
    }

    public void addParticipant(String roomCode, String username) {
        redisTemplate.opsForSet().add(presenceKey(roomCode), username);
        redisTemplate.expire(presenceKey(roomCode), DOC_TTL);
    }

    public void removeParticipant(String roomCode, String username) {
        redisTemplate.opsForSet().remove(presenceKey(roomCode), username);
    }

    public Set<String> getParticipants(String roomCode) {
        Set<String> members = redisTemplate.opsForSet().members(presenceKey(roomCode));
        return members != null ? members : Set.of();
    }
}
