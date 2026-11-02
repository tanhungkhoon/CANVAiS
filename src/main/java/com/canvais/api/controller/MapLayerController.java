package com.canvais.api.controller;

import com.canvais.api.dto.request.CreateEntityRequest;
import com.canvais.api.dto.response.GraphicalEntityResponse;
import com.canvais.service.GraphicalEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for plotting and retrieving graphical entities on layers.
 */
@RestController
@RequestMapping("/api/v1/layers")
@Tag(name = "Map Layers", description = "Endpoints for layer entities and GeoJSON rendering")
public class MapLayerController {

    private final GraphicalEntityService entityService;

    public MapLayerController(GraphicalEntityService entityService) {
        this.entityService = entityService;
    }

    /**
     * Plots a new graphical entity onto a layer.
     *
     * @param layerId layer UUID
     * @param request entity creation parameters
     * @return 201 Created with plotted entity
     */
    @PostMapping("/{layerId}/entities")
    @Operation(summary = "Plot a new graphical entity onto a layer")
    public ResponseEntity<GraphicalEntityResponse> createEntity(@PathVariable UUID layerId,
                                                                @Valid @RequestBody CreateEntityRequest request) {
        GraphicalEntityResponse created = entityService.createEntity(layerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves all entities on a layer in standard GeoJSON FeatureCollection format.
     *
     * @param layerId layer UUID
     * @return GeoJSON FeatureCollection
     */
    @GetMapping(value = "/{layerId}/entities", produces = "application/geo+json")
    @Operation(summary = "Get all graphical entities in a layer as GeoJSON FeatureCollection")
    public ResponseEntity<Map<String, Object>> getLayerGeoJson(@PathVariable UUID layerId) {
        return ResponseEntity.ok(entityService.getLayerFeatureCollection(layerId));
    }
}
