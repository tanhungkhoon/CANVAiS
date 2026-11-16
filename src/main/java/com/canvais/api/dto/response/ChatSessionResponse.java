package com.canvais.api.dto.response;

import com.canvais.domain.model.ChatSession;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload describing a chat session.
 */
public class ChatSessionResponse {

    private UUID id;
    private UUID mapSessionId;
    private UUID targetLayerId;
    private String title;
    private String activeModel;
    private Instant createdAt;

    public ChatSessionResponse() {
    }

    public ChatSessionResponse(UUID id, UUID mapSessionId, UUID targetLayerId, String title, String activeModel, Instant createdAt) {
        this.id = id;
        this.mapSessionId = mapSessionId;
        this.targetLayerId = targetLayerId;
        this.title = title;
        this.activeModel = activeModel;
        this.createdAt = createdAt;
    }

    public static ChatSessionResponse fromEntity(ChatSession session) {
        if (session == null) {
            return null;
        }
        return new ChatSessionResponse(
                session.getId(),
                session.getMapSessionId(),
                session.getTargetLayerId(),
                session.getTitle(),
                session.getActiveModel(),
                session.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMapSessionId() {
        return mapSessionId;
    }

    public void setMapSessionId(UUID mapSessionId) {
        this.mapSessionId = mapSessionId;
    }

    public UUID getTargetLayerId() {
        return targetLayerId;
    }

    public void setTargetLayerId(UUID targetLayerId) {
        this.targetLayerId = targetLayerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getActiveModel() {
        return activeModel;
    }

    public void setActiveModel(String activeModel) {
        this.activeModel = activeModel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
