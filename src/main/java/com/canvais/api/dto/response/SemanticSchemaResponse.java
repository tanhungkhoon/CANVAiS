package com.canvais.api.dto.response;

import com.canvais.domain.model.EntityTypeDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response payload carrying semantic schema definitions.
 */
public class SemanticSchemaResponse {

    private UUID id;
    private String name;
    private String domainCategory;
    private String version;
    private List<EntityTypeDefinition> entityTypes = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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
