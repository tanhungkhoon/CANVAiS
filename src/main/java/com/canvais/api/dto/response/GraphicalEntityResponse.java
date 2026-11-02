package com.canvais.api.dto.response;

import com.canvais.domain.model.EntityStyle;
import com.canvais.domain.model.GeometryType;
import com.canvais.domain.model.ProvenanceType;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO representing a plotted graphical entity.
 */
public class GraphicalEntityResponse {

    private UUID id;
    private UUID layerId;
    private String name;
    private String entityType;
    private GeometryType geometryType;
    private Map<String, Object> geometry;
    private EntityStyle style;
    private Map<String, Object> semanticAttributes;
    private Double confidenceScore;
    private ProvenanceType provenance;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getLayerId() {
        return layerId;
    }

    public void setLayerId(UUID layerId) {
        this.layerId = layerId;
    }

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

    public GeometryType getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(GeometryType geometryType) {
        this.geometryType = geometryType;
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

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public ProvenanceType getProvenance() {
        return provenance;
    }

    public void setProvenance(ProvenanceType provenance) {
        this.provenance = provenance;
    }
}
