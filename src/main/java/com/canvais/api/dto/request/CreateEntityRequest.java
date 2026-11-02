package com.canvais.api.dto.request;

import com.canvais.domain.model.EntityStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Request payload for plotting a graphical entity onto a layer.
 */
public class CreateEntityRequest {

    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Entity type is required")
    private String entityType;

    @NotNull(message = "GeoJSON geometry is required")
    private Map<String, Object> geometry;

    private EntityStyle style;

    private Map<String, Object> semanticAttributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Map<String, Object> getGeometry() {
        return geometry;
    }

    public void setGeometry(Map<String, Object> geometry) {
        this.geometry = geometry;
    }

    public EntityStyle getStyle() {
        return style;
    }

    public void setStyle(EntityStyle style) {
        this.style = style;
    }

    public Map<String, Object> getSemanticAttributes() {
        return semanticAttributes;
    }

    public void setSemanticAttributes(Map<String, Object> semanticAttributes) {
        this.semanticAttributes = semanticAttributes;
    }
}
