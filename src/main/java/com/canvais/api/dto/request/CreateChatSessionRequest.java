package com.canvais.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for creating or retrieving a chat session associated with a map session.
 */
public class CreateChatSessionRequest {

    @NotNull
    private UUID mapSessionId;

    @Size(max = 100)
    private String title;

    public CreateChatSessionRequest() {
    }

    public CreateChatSessionRequest(UUID mapSessionId, String title) {
        this.mapSessionId = mapSessionId;
        this.title = title;
    }

    public UUID getMapSessionId() {
        return mapSessionId;
    }

    public void setMapSessionId(UUID mapSessionId) {
        this.mapSessionId = mapSessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
