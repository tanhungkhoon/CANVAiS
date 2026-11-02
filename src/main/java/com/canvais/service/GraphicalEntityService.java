package com.canvais.service;

import com.canvais.api.dto.request.CreateEntityRequest;
import com.canvais.api.dto.response.GraphicalEntityResponse;
import com.canvais.domain.model.EntityStyle;
import com.canvais.domain.model.GeometryType;
import com.canvais.domain.model.GraphicalEntity;
import com.canvais.domain.model.ProvenanceType;
import com.canvais.domain.repository.GraphicalEntityRepository;
import com.canvais.domain.repository.MapLayerRepository;
import com.canvais.infrastructure.geospatial.GeoJsonConverter;
import com.canvais.infrastructure.geospatial.JtsGeometryAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing graphical entity plotting, spatial verification, and GeoJSON export.
 */
@Service
@Transactional
public class GraphicalEntityService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GraphicalEntityRepository entityRepository;
    private final MapLayerRepository layerRepository;
    private final JtsGeometryAdapter geometryAdapter;
    private final GeoJsonConverter geoJsonConverter;

    public GraphicalEntityService(GraphicalEntityRepository entityRepository,
                                  MapLayerRepository layerRepository,
                                  JtsGeometryAdapter geometryAdapter,
                                  GeoJsonConverter geoJsonConverter) {
        this.entityRepository = entityRepository;
        this.layerRepository = layerRepository;
        this.geometryAdapter = geometryAdapter;
        this.geoJsonConverter = geoJsonConverter;
    }

    /**
     * Plots a new entity onto the specified layer.
     *
     * @param layerId target layer UUID
     * @param request entity creation parameters
     * @return created entity response DTO
     */
    public GraphicalEntityResponse createEntity(UUID layerId, CreateEntityRequest request) {
        if (!layerRepository.existsById(layerId)) {
            throw new NoSuchElementException("MapLayer not found with ID: " + layerId);
        }

        // Validate and parse geometry via JTS
        Geometry jtsGeom = geometryAdapter.parseGeoJson(request.getGeometry());
        if (!jtsGeom.isValid()) {
            throw new IllegalArgumentException("Invalid topological geometry: " + jtsGeom.getGeometryType());
        }

        GeometryType geomType = mapGeometryType(jtsGeom.getGeometryType());

        String geoJsonStr;
        try {
            geoJsonStr = OBJECT_MAPPER.writeValueAsString(request.getGeometry());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize GeoJSON geometry", e);
        }

        GraphicalEntity entity = new GraphicalEntity(layerId, request.getEntityType(), geomType, geoJsonStr);
        entity.setName(request.getName());
        if (request.getStyle() != null) {
            entity.setStyle(request.getStyle());
        } else {
            entity.setStyle(new EntityStyle());
        }
        entity.setSemanticAttributes(request.getSemanticAttributes());
        entity.setProvenance(ProvenanceType.MANUAL);

        GraphicalEntity saved = entityRepository.save(entity);
        return toResponse(saved);
    }

    /**
     * Retrieves all entities in a layer as a GeoJSON FeatureCollection map.
     *
     * @param layerId layer UUID
     * @return GeoJSON FeatureCollection
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLayerFeatureCollection(UUID layerId) {
        if (!layerRepository.existsById(layerId)) {
            throw new NoSuchElementException("MapLayer not found with ID: " + layerId);
        }
        List<GraphicalEntity> entities = entityRepository.findByLayerId(layerId);
        List<Map<String, Object>> features = new ArrayList<>();

        for (GraphicalEntity entity : entities) {
            Map<String, Object> geomMap = parseGeoJsonSafe(entity.getGeometryGeoJson());
            Map<String, Object> properties = new HashMap<>(entity.getSemanticAttributes());
            properties.put("name", entity.getName());
            properties.put("entityType", entity.getEntityType());
            properties.put("provenance", entity.getProvenance().name());
            if (entity.getStyle() != null) {
                properties.put("strokeColor", entity.getStyle().getStrokeColor());
                properties.put("fillColor", entity.getStyle().getFillColor());
                properties.put("strokeWidth", entity.getStyle().getStrokeWidth());
            }
            features.add(geoJsonConverter.toFeature(entity.getId().toString(), geomMap, properties));
        }

        return geoJsonConverter.toFeatureCollection(features);
    }

    /**
     * Lists all entities in a layer as response DTOs.
     *
     * @param layerId layer UUID
     * @return list of response DTOs
     */
    @Transactional(readOnly = true)
    public List<GraphicalEntityResponse> listEntitiesByLayer(UUID layerId) {
        return entityRepository.findByLayerId(layerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts entity to response DTO.
     */
    public GraphicalEntityResponse toResponse(GraphicalEntity entity) {
        GraphicalEntityResponse response = new GraphicalEntityResponse();
        response.setId(entity.getId());
        response.setLayerId(entity.getLayerId());
        response.setName(entity.getName());
        response.setEntityType(entity.getEntityType());
        response.setGeometryType(entity.getGeometryType());
        response.setGeometry(parseGeoJsonSafe(entity.getGeometryGeoJson()));
        response.setStyle(entity.getStyle());
        response.setSemanticAttributes(entity.getSemanticAttributes());
        response.setConfidenceScore(entity.getConfidenceScore());
        response.setProvenance(entity.getProvenance());
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGeoJsonSafe(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private GeometryType mapGeometryType(String jtsType) {
        return switch (jtsType.toUpperCase()) {
            case "POINT" -> GeometryType.POINT;
            case "LINESTRING" -> GeometryType.LINE_STRING;
            case "POLYGON" -> GeometryType.POLYGON;
            default -> GeometryType.MULTI_POLYGON;
        };
    }
}
