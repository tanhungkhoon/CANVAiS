package com.canvais.domain.model;

import jakarta.persistence.Embeddable;

/**
 * Visual styling configuration for a plotted graphical entity.
 */
@Embeddable
public class EntityStyle {

    private String strokeColor;
    private String fillColor;
    private Float strokeWidth;
    private String dashArray;
    private String icon;

    /**
     * Default constructor with sensible default cartographic styles.
     */
    public EntityStyle() {
        this.strokeColor = "#2A9D8F";
        this.fillColor = "#2A9D8F33";
        this.strokeWidth = 2.0f;
    }

    /**
     * Parameterized constructor for styling.
     *
     * @param strokeColor hex or rgba stroke color
     * @param fillColor hex or rgba fill color
     * @param strokeWidth line width in pixels
     * @param dashArray stroke dash pattern
     * @param icon symbol icon identifier
     */
    public EntityStyle(String strokeColor, String fillColor, Float strokeWidth, String dashArray, String icon) {
        this.strokeColor = strokeColor != null ? strokeColor : "#2A9D8F";
        this.fillColor = fillColor != null ? fillColor : "#2A9D8F33";
        this.strokeWidth = strokeWidth != null ? strokeWidth : 2.0f;
        this.dashArray = dashArray;
        this.icon = icon;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public Float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(Float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public String getDashArray() {
        return dashArray;
    }

    public void setDashArray(String dashArray) {
        this.dashArray = dashArray;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
