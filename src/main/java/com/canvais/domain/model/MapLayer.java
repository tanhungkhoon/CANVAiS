package com.canvais.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing an organizational and semantic drawing layer on a base map.
 */
@Entity
@Table(name = "map_layers")
public class MapLayer {

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID sessionId;

    @NotNull
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String name;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer orderIndex;

    @NotNull
    @Column(nullable = false)
    private Boolean visible;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Column(nullable = false)
    private Double opacity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LayerType layerType;

    private UUID schemaId;

    private String pluginId;

    @OneToMany(mappedBy = "layerId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GraphicalEntity> entities = new ArrayList<>();

    /**
     * Default constructor.
     */
    public MapLayer() {
        this.id = UUID.randomUUID();
        this.visible = true;
        this.opacity = 1.0;
        this.orderIndex = 0;
        this.layerType = LayerType.STANDARD;
    }

    /**
     * Parameterized constructor.
     *
     * @param sessionId parent session UUID
     * @param name layer title
     * @param orderIndex z-index order
     * @param layerType operational layer type
     */
    public MapLayer(UUID sessionId, String name, Integer orderIndex, LayerType layerType) {
        this();
        this.sessionId = sessionId;
        this.name = name;
        if (orderIndex != null) {
            this.orderIndex = orderIndex;
        }
        if (layerType != null) {
            this.layerType = layerType;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.visible == null) {
            this.visible = true;
        }
        if (this.opacity == null) {
            this.opacity = 1.0;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

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

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
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

    public List<GraphicalEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<GraphicalEntity> entities) {
        this.entities = entities;
    }
}
