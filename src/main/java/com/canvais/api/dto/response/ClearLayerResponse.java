package com.canvais.api.dto.response;

import java.util.UUID;

/**
 * Response payload confirming that an overlay layer has been cleared.
 */
public class ClearLayerResponse {

    private UUID layerId;
    private int clearedEntitiesCount;

    public ClearLayerResponse() {
    }

    public ClearLayerResponse(UUID layerId, int clearedEntitiesCount) {
        this.layerId = layerId;
        this.clearedEntitiesCount = clearedEntitiesCount;
    }

    public UUID getLayerId() {
        return layerId;
    }

    public void setLayerId(UUID layerId) {
        this.layerId = layerId;
    }

    public int getClearedEntitiesCount() {
        return clearedEntitiesCount;
    }

    public void setClearedEntitiesCount(int clearedEntitiesCount) {
        this.clearedEntitiesCount = clearedEntitiesCount;
    }
}
