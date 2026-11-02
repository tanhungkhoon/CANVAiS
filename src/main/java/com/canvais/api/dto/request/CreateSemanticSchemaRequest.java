package com.canvais.api.dto.request;

import com.canvais.domain.model.EntityTypeDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for creating a custom SemanticSchema.
 */
public class CreateSemanticSchemaRequest {

    @NotBlank(message = "Schema name is required")
    private String name;

    @NotBlank(message = "Domain category is required")
    private String domainCategory;

    private String version = "1.0.0";

    @NotEmpty(message = "At least one entity type definition is required")
    private List<EntityTypeDefinition> entityTypes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainCategory() {
        return domainCategory;
    }

    public void setDomainCategory(String domainCategory) {
        this.domainCategory = domainCategory;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<EntityTypeDefinition> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<EntityTypeDefinition> entityTypes) {
        this.entityTypes = entityTypes;
    }
}
