package com.canvais.api.controller;

import com.canvais.api.dto.request.CreateSemanticSchemaRequest;
import com.canvais.api.dto.response.SemanticSchemaResponse;
import com.canvais.service.SemanticSchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing custom semantic schemas and layer ontologies.
 */
@RestController
@RequestMapping("/api/v1/integrations/schemas")
@Tag(name = "Semantic Schemas", description = "Endpoints for defining custom layer entity ontologies")
public class SemanticSchemaController {

    private final SemanticSchemaService schemaService;

    public SemanticSchemaController(SemanticSchemaService schemaService) {
        this.schemaService = schemaService;
    }

    /**
     * Lists all registered custom semantic schemas.
     *
     * @return 200 OK with list of schemas
     */
    @GetMapping
    @Operation(summary = "List available semantic schemas")
    public ResponseEntity<List<SemanticSchemaResponse>> listSchemas() {
        return ResponseEntity.ok(schemaService.listSchemas());
    }

    /**
     * Registers a new custom semantic schema.
     *
     * @param request creation request
     * @return 201 Created with schema details
     */
    @PostMapping
    @Operation(summary = "Define a new custom semantic schema")
    public ResponseEntity<SemanticSchemaResponse> createSchema(@Valid @RequestBody CreateSemanticSchemaRequest request) {
        SemanticSchemaResponse created = schemaService.createSchema(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
