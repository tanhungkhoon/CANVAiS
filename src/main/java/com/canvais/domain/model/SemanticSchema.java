package com.canvais.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity holding custom layer ontologies and validation schemas.
 */
@Entity
@Table(name = "semantic_schemas")
public class SemanticSchema {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @NotNull
    private UUID id;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String domainCategory;

    @NotNull
    @Column(nullable = false, length = 20)
    private String version;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String entityTypesJson;

    public SemanticSchema() {
        this.id = UUID.randomUUID();
        this.version = "1.0.0";
    }

    public SemanticSchema(String name, String domainCategory, String version) {
        this();
        this.name = name;
        this.domainCategory = domainCategory;
        if (version != null) {
            this.version = version;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.version == null) {
            this.version = "1.0.0";
        }
    }

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
        if (entityTypesJson == null || entityTypesJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(entityTypesJson, new TypeReference<List<EntityTypeDefinition>>() { });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setEntityTypes(List<EntityTypeDefinition> entityTypes) {
        if (entityTypes == null || entityTypes.isEmpty()) {
            this.entityTypesJson = null;
        } else {
            try {
                this.entityTypesJson = OBJECT_MAPPER.writeValueAsString(entityTypes);
            } catch (JsonProcessingException e) {
                this.entityTypesJson = null;
            }
        }
    }
}
