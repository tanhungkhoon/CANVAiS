package com.canvais.api.dto.request;

import com.canvais.domain.model.LayerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for creating a layer within a session.
 */
public class CreateLayerRequest {

    @NotBlank(message = "Layer name is required")
    @Size(max = 80, message = "Layer name cannot exceed 80 characters")
    private String name;

    private Integer orderIndex = 0;

    @NotNull(message = "Layer type is required")
    private LayerType layerType = LayerType.STANDARD;

    private UUID schemaId;

    private String pluginId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public LayerType getLayerType() {
        return layerType;
    }

    public void setLayerType(LayerType layerType) {
        this.layerType = layerType;
    }

    public UUID getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(UUID schemaId) {
        this.schemaId = schemaId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
}
