package com.canvais.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new MapSession.
 */
public class CreateSessionRequest {

    @NotBlank(message = "Session title is required")
    @Size(max = 120, message = "Title cannot exceed 120 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Min(value = -90, message = "Latitude must be >= -90")
    @Max(value = 90, message = "Latitude must be <= 90")
    private Double centerLat = 48.1800;

    @Min(value = -180, message = "Longitude must be >= -180")
    @Max(value = 180, message = "Longitude must be <= 180")
    private Double centerLng = 8.2000;

    @Min(value = 1, message = "Zoom level must be >= 1")
    @Max(value = 22, message = "Zoom level must be <= 22")
    private Integer zoomLevel = 12;

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
}
