package com.canvais.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of an individual attribute within an entity type schema.
 */
public class AttributeDefinition {

    private String name;
    private DataType dataType;
    private Boolean required;
    private List<String> allowedValues = new ArrayList<>();

    public AttributeDefinition() {
        this.dataType = DataType.STRING;
        this.required = false;
    }

    public AttributeDefinition(String name, DataType dataType, Boolean required, List<String> allowedValues) {
        this.name = name;
        this.dataType = dataType;
        this.required = required;
        if (allowedValues != null) {
            this.allowedValues = allowedValues;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }
}
