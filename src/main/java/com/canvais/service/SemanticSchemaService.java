package com.canvais.service;

import com.canvais.api.dto.request.CreateSemanticSchemaRequest;
import com.canvais.api.dto.response.SemanticSchemaResponse;
import com.canvais.domain.model.AttributeDefinition;
import com.canvais.domain.model.EntityTypeDefinition;
import com.canvais.domain.model.SemanticSchema;
import com.canvais.domain.repository.SemanticSchemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing semantic schemas and ontology validation for custom layers.
 */
@Service
@Transactional
public class SemanticSchemaService {

    private final SemanticSchemaRepository schemaRepository;

    public SemanticSchemaService(SemanticSchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    /**
     * Registers a new semantic schema.
     *
     * @param request creation request
     * @return response DTO
     */
    public SemanticSchemaResponse createSchema(CreateSemanticSchemaRequest request) {
        SemanticSchema schema = new SemanticSchema(request.getName(), request.getDomainCategory(), request.getVersion());
        schema.setEntityTypes(request.getEntityTypes());

        SemanticSchema saved = schemaRepository.save(schema);
        return toResponse(saved);
    }

    /**
     * Lists all registered semantic schemas.
     *
     * @return list of schema responses
     */
    @Transactional(readOnly = true)
    public List<SemanticSchemaResponse> listSchemas() {
        return schemaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validates an entity's semantic attributes against the specified schema.
     *
     * @param schemaId schema UUID
     * @param entityType entity classification
     * @param attributes entity key-value attributes
     */
    @Transactional(readOnly = true)
    public void validateEntityAttributes(UUID schemaId, String entityType, Map<String, Object> attributes) {
        if (schemaId == null) {
            return; // Standard layers without custom schema bypass validation
        }

        SemanticSchema schema = schemaRepository.findById(schemaId)
                .orElseThrow(() -> new NoSuchElementException("SemanticSchema not found with ID: " + schemaId));

        Optional<EntityTypeDefinition> matchingType = schema.getEntityTypes().stream()
                .filter(t -> t.getName().equalsIgnoreCase(entityType))
                .findFirst();

        if (matchingType.isEmpty()) {
            throw new IllegalArgumentException("Entity type '" + entityType + "' is not allowed in schema: " + schema.getName());
        }

        EntityTypeDefinition def = matchingType.get();
        if (def.getAttributes() != null) {
            for (AttributeDefinition attr : def.getAttributes()) {
                if (Boolean.TRUE.equals(attr.getRequired())) {
                    if (attributes == null || !attributes.containsKey(attr.getName())) {
                        throw new IllegalArgumentException("Missing required attribute '" + attr.getName() + "' for entity: " + entityType);
                    }
                }
            }
        }
    }

    /**
     * Maps SemanticSchema entity to response DTO.
     */
    public SemanticSchemaResponse toResponse(SemanticSchema schema) {
        SemanticSchemaResponse response = new SemanticSchemaResponse();
        response.setId(schema.getId());
        response.setName(schema.getName());
        response.setDomainCategory(schema.getDomainCategory());
        response.setVersion(schema.getVersion());
        response.setEntityTypes(schema.getEntityTypes());
        return response;
    }
}
