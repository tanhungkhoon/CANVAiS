package com.canvais.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an individual message turn within a chat session.
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Column(name = "chat_session_id", nullable = false)
    private UUID chatSessionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRole role;

    @NotNull
    @Size(min = 1, max = 4000)
    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "vibe_record_id")
    private UUID vibeRecordId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ChatActionType actionType = ChatActionType.NONE;

    @Size(max = 2000)
    @Column(length = 2000)
    private String metadata;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    public ChatMessage() {
    }

    public ChatMessage(UUID id, UUID chatSessionId, ChatRole role, String content,
                       UUID vibeRecordId, ChatActionType actionType, String metadata) {
        this.id = id != null ? id : UUID.randomUUID();
        this.chatSessionId = chatSessionId;
        this.role = role;
        this.content = content;
        this.vibeRecordId = vibeRecordId;
        this.actionType = actionType != null ? actionType : ChatActionType.NONE;
        this.metadata = metadata;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(UUID chatSessionId) {
        this.chatSessionId = chatSessionId;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
