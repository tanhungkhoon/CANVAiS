package com.canvais.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a conversational sketch session linked to a map session and an overlay layer.
 */
@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Column(name = "map_session_id", nullable = false)
    private UUID mapSessionId;

    @NotNull
    @Column(name = "target_layer_id", nullable = false)
    private UUID targetLayerId;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String title = "Chat Sketching Session";

    @NotNull
    @Size(min = 1, max = 80)
    @Column(name = "active_model", nullable = false, length = 80)
    private String activeModel = "llama3.2";

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ChatSession() {
    }

    public ChatSession(UUID id, UUID mapSessionId, UUID targetLayerId, String title, String activeModel) {
        this.id = id != null ? id : UUID.randomUUID();
        this.mapSessionId = mapSessionId;
        this.targetLayerId = targetLayerId;
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (activeModel != null && !activeModel.isBlank()) {
            this.activeModel = activeModel;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
