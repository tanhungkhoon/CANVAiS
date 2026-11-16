package com.canvais.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for sending a user prompt to the chat assistant.
 */
public class SendChatMessageRequest {

    @NotBlank
    @Size(max = 4000)
    private String message;

    private UUID vibeRecordId;

    private BoundingBoxDto viewportBounds;

    private GesturePointDto centerCoordinates;

    public SendChatMessageRequest() {
    }

    public SendChatMessageRequest(String message, UUID vibeRecordId, BoundingBoxDto viewportBounds, GesturePointDto centerCoordinates) {
        this.message = message;
        this.vibeRecordId = vibeRecordId;
        this.viewportBounds = viewportBounds;
        this.centerCoordinates = centerCoordinates;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getVibeRecordId() {
        return vibeRecordId;
    }

    public void setVibeRecordId(UUID vibeRecordId) {
        this.vibeRecordId = vibeRecordId;
    }

    public BoundingBoxDto getViewportBounds() {
        return viewportBounds;
    }

    public void setViewportBounds(BoundingBoxDto viewportBounds) {
        this.viewportBounds = viewportBounds;
    }

    public GesturePointDto getCenterCoordinates() {
        return centerCoordinates;
    }

    public void setCenterCoordinates(GesturePointDto centerCoordinates) {
        this.centerCoordinates = centerCoordinates;
    }
}
