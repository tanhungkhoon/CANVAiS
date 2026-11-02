package com.canvais.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request payload submitting vibe text and freehand gesture strokes to generate a spatial proposal.
 */
public class VibeIntentProposeRequest {

    @NotNull(message = "Session ID is required")
    private UUID sessionId;

    @NotNull(message = "Target Layer ID is required")
    private UUID targetLayerId;

    @NotBlank(message = "Vibe text description is required")
    private String textVibe;

    private List<GesturePointDto> gestureStrokes = new ArrayList<>();

    private BoundingBoxDto viewportBounds;

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getTargetLayerId() {
        return targetLayerId;
    }

    public void setTargetLayerId(UUID targetLayerId) {
        this.targetLayerId = targetLayerId;
    }

    public String getTextVibe() {
        return textVibe;
    }

    public void setTextVibe(String textVibe) {
        this.textVibe = textVibe;
    }

    public List<GesturePointDto> getGestureStrokes() {
        return gestureStrokes;
    }

    public void setGestureStrokes(List<GesturePointDto> gestureStrokes) {
        this.gestureStrokes = gestureStrokes;
    }

    public BoundingBoxDto getViewportBounds() {
        return viewportBounds;
    }

    public void setViewportBounds(BoundingBoxDto viewportBounds) {
        this.viewportBounds = viewportBounds;
    }
}
