package com.canvais.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain entity representing a discrete plotted graphical object on a map layer.
 */
@Entity
@Table(name = "graphical_entities")
public class GraphicalEntity {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID layerId;

    @Size(max = 100)
    @Column(length = 100)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String entityType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeometryType geometryType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String geometryGeoJson;

    @Embedded
    private EntityStyle style;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String semanticAttributesJson;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidenceScore;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProvenanceType provenance;

    /**
     * Default constructor.
     */
    public GraphicalEntity() {
        this.id = UUID.randomUUID();
        this.style = new EntityStyle();
        this.provenance = ProvenanceType.MANUAL;
    }

    /**
     * Parameterized constructor.
     *
     * @param layerId parent layer UUID
     * @param entityType semantic classification
     * @param geometryType geometric primitive type
     * @param geometryGeoJson GeoJSON geometry string
     */
    public GraphicalEntity(UUID layerId, String entityType, GeometryType geometryType, String geometryGeoJson) {
        this();
        this.layerId = layerId;
        this.entityType = entityType;
        this.geometryType = geometryType;
        this.geometryGeoJson = geometryGeoJson;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.style == null) {
            this.style = new EntityStyle();
        }
        if (this.provenance == null) {
            this.provenance = ProvenanceType.MANUAL;
        }
    }

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

    public String getGeometryGeoJson() {
        return geometryGeoJson;
    }

    public void setGeometryGeoJson(String geometryGeoJson) {
        this.geometryGeoJson = geometryGeoJson;
    }

    public EntityStyle getStyle() {
        return style;
    }

    public void setStyle(EntityStyle style) {
        this.style = style;
    }

    public Map<String, Object> getSemanticAttributes() {
        if (semanticAttributesJson == null || semanticAttributesJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(semanticAttributesJson, new TypeReference<Map<String, Object>>() { });
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    public void setSemanticAttributes(Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            this.semanticAttributesJson = null;
        } else {
            try {
                this.semanticAttributesJson = OBJECT_MAPPER.writeValueAsString(attributes);
            } catch (JsonProcessingException e) {
                this.semanticAttributesJson = null;
            }
        }
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
