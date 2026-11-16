package com.canvais.api.dto.response;

import com.canvais.domain.model.ChatActionType;
import com.canvais.domain.model.ChatMessage;
import com.canvais.domain.model.ChatRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Data transfer object representing a chat message.
 */
public class ChatMessageDto {

    private UUID id;
    private ChatRole role;
    private String content;
    private UUID vibeRecordId;
    private ChatActionType actionType;
    private Instant timestamp;

    public ChatMessageDto() {
    }

    public ChatMessageDto(UUID id, ChatRole role, String content, UUID vibeRecordId, ChatActionType actionType, Instant timestamp) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.vibeRecordId = vibeRecordId;
        this.actionType = actionType;
        this.timestamp = timestamp;
    }

    public static ChatMessageDto fromEntity(ChatMessage message) {
        if (message == null) {
            return null;
        }
        return new ChatMessageDto(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getVibeRecordId(),
                message.getActionType(),
                message.getTimestamp()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ChatRole getRole() {
        return role;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getVibeRecordId() {
        return vibeRecordId;
    }

    public void setVibeRecordId(UUID vibeRecordId) {
        this.vibeRecordId = vibeRecordId;
    }

    public ChatActionType getActionType() {
        return actionType;
    }

    public void setActionType(ChatActionType actionType) {
        this.actionType = actionType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
