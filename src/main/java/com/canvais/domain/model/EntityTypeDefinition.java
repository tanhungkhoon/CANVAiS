package com.canvais.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of an allowed entity classification and its attribute constraints.
 */
public class EntityTypeDefinition {

    private String name;
    private String displayName;
    private List<GeometryType> allowedGeometries = new ArrayList<>();
    private EntityStyle defaultStyle;
    private List<AttributeDefinition> attributes = new ArrayList<>();

    public EntityTypeDefinition() {
        this.defaultStyle = new EntityStyle();
    }

    public EntityTypeDefinition(String name, String displayName, List<GeometryType> allowedGeometries) {
        this();
        this.name = name;
        this.displayName = displayName;
        if (allowedGeometries != null) {
            this.allowedGeometries = allowedGeometries;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<GeometryType> getAllowedGeometries() {
        return allowedGeometries;
    }

    public void setAllowedGeometries(List<GeometryType> allowedGeometries) {
        this.allowedGeometries = allowedGeometries;
    }

    public EntityStyle getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(EntityStyle defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDefinition> attributes) {
        this.attributes = attributes;
    }
}
