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
 * Domain entity recording graphical entity actions executed during a chat turn for auditability and refinement.
 */
@Entity
@Table(name = "chat_entity_actions")
public class ChatEntityAction {

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @NotNull
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ChatActionType actionType;

    @Size(max = 4000)
    @Column(name = "previous_state_json", length = 4000)
    private String previousStateJson;

    @NotNull
    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt;

    public ChatEntityAction() {
    }

    public ChatEntityAction(UUID id, UUID messageId, UUID entityId, ChatActionType actionType, String previousStateJson) {
        this.id = id != null ? id : UUID.randomUUID();
        this.messageId = messageId;
        this.entityId = entityId;
        this.actionType = actionType;
        this.previousStateJson = previousStateJson;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (executedAt == null) {
            executedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public ChatActionType getActionType() {
        return actionType;
    }

    public void setActionType(ChatActionType actionType) {
        this.actionType = actionType;
    }

    public String getPreviousStateJson() {
        return previousStateJson;
    }

    public void setPreviousStateJson(String previousStateJson) {
        this.previousStateJson = previousStateJson;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }
}
