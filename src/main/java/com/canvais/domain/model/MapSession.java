package com.canvais.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing an active geospatial map workspace session.
 */
@Entity
@Table(name = "map_sessions")
public class MapSession {

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String title;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @Min(-90)
    @Max(90)
    @Column(nullable = false)
    private Double centerLat;

    @NotNull
    @Min(-180)
    @Max(180)
    @Column(nullable = false)
    private Double centerLng;

    @NotNull
    @Min(1)
    @Max(22)
    @Column(nullable = false)
    private Integer zoomLevel;

    @NotNull
    @Column(nullable = false)
    private String crs;

    @NotNull
    @Column(nullable = false)
    private Instant createdAt;

    @NotNull
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "sessionId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<MapLayer> layers = new ArrayList<>();

    /**
     * Default constructor for JPA.
     */
    public MapSession() {
        this.id = UUID.randomUUID();
        this.crs = "EPSG:4326";
        this.zoomLevel = 12;
        this.centerLat = 48.1800;
        this.centerLng = 8.2000;
    }

    /**
     * Parameterized constructor.
     *
     * @param title session title
     * @param description optional brief
     * @param centerLat latitude
     * @param centerLng longitude
     * @param zoomLevel zoom level
     */
    public MapSession(String title, String description, Double centerLat, Double centerLng, Integer zoomLevel) {
        this();
        this.title = title;
        this.description = description;
        if (centerLat != null) {
            this.centerLat = centerLat;
        }
        if (centerLng != null) {
            this.centerLng = centerLng;
        }
        if (zoomLevel != null) {
            this.zoomLevel = zoomLevel;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }

    public Double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(Double centerLng) {
        this.centerLng = centerLng;
    }

    public Integer getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(Integer zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MapLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<MapLayer> layers) {
        this.layers = layers;
    }
}
