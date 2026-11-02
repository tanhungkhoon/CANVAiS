package com.canvais.api.dto.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed session response DTO including its contained layers.
 */
public class MapSessionDetailResponse extends MapSessionResponse {

    private List<MapLayerResponse> layers = new ArrayList<>();

    public List<MapLayerResponse> getLayers() {
        return layers;
    }

    public void setLayers(List<MapLayerResponse> layers) {
        this.layers = layers;
    }
}
