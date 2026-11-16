package com.canvais.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for ingesting an existing vibe record into the chat conversation stream.
 */
public class IngestVibeToChatRequest {

    private UUID vibeRecordId;

    @NotBlank
    private String vibeTranscript;

    private List<GesturePointDto> gesturePoints;

    private BoundingBoxDto viewportBounds;

    public IngestVibeToChatRequest() {
    }

    public IngestVibeToChatRequest(UUID vibeRecordId, String vibeTranscript, List<GesturePointDto> gesturePoints, BoundingBoxDto viewportBounds) {
        this.vibeRecordId = vibeRecordId;
        this.vibeTranscript = vibeTranscript;
        this.gesturePoints = gesturePoints;
        this.viewportBounds = viewportBounds;
    }

    public UUID getVibeRecordId() {
        return vibeRecordId;
    }

    public void setVibeRecordId(UUID vibeRecordId) {
        this.vibeRecordId = vibeRecordId;
    }

    public String getVibeTranscript() {
        return vibeTranscript;
    }

    public void setVibeTranscript(String vibeTranscript) {
        this.vibeTranscript = vibeTranscript;
    }

    public List<GesturePointDto> getGesturePoints() {
        return gesturePoints;
    }

    public void setGesturePoints(List<GesturePointDto> gesturePoints) {
        this.gesturePoints = gesturePoints;
    }

    public BoundingBoxDto getViewportBounds() {
        return viewportBounds;
    }

    public void setViewportBounds(BoundingBoxDto viewportBounds) {
        this.viewportBounds = viewportBounds;
    }
}
