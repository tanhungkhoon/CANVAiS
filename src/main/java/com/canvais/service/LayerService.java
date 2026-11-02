package com.canvais.service;

import com.canvais.api.dto.request.CreateLayerRequest;
import com.canvais.api.dto.response.MapLayerResponse;
import com.canvais.domain.model.MapLayer;
import com.canvais.domain.repository.MapLayerRepository;
import com.canvais.domain.repository.MapSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing layer creation, reordering, and querying within sessions.
 */
@Service
@Transactional
public class LayerService {

    private final MapLayerRepository layerRepository;
    private final MapSessionRepository sessionRepository;

    public LayerService(MapLayerRepository layerRepository, MapSessionRepository sessionRepository) {
        this.layerRepository = layerRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Creates a new layer within the specified session.
     *
     * @param sessionId parent session UUID
     * @param request layer creation request
     * @return created layer response DTO
     */
    public MapLayerResponse createLayer(UUID sessionId, CreateLayerRequest request) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new NoSuchElementException("MapSession not found with ID: " + sessionId);
        }
        MapLayer layer = new MapLayer(sessionId, request.getName(), request.getOrderIndex(), request.getLayerType());
        layer.setSchemaId(request.getSchemaId());
        layer.setPluginId(request.getPluginId());

        MapLayer saved = layerRepository.save(layer);
        return toResponse(saved);
    }

    /**
     * Retrieves all layers for a given session.
     *
     * @param sessionId session UUID
     * @return ordered list of layer responses
     */
    @Transactional(readOnly = true)
    public List<MapLayerResponse> listLayersBySession(UUID sessionId) {
        return layerRepository.findBySessionIdOrderByOrderIndexAsc(sessionId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a layer by its UUID.
     *
     * @param layerId layer UUID
     * @return layer entity
     */
    @Transactional(readOnly = true)
    public MapLayer getLayer(UUID layerId) {
        return layerRepository.findById(layerId)
                .orElseThrow(() -> new NoSuchElementException("MapLayer not found with ID: " + layerId));
    }

    /**
     * Maps MapLayer entity to DTO.
     */
    public MapLayerResponse toResponse(MapLayer layer) {
        MapLayerResponse response = new MapLayerResponse();
        response.setId(layer.getId());
        response.setSessionId(layer.getSessionId());
        response.setName(layer.getName());
        response.setOrderIndex(layer.getOrderIndex());
        response.setVisible(layer.getVisible());
        response.setOpacity(layer.getOpacity());
        response.setLayerType(layer.getLayerType());
        response.setSchemaId(layer.getSchemaId());
        response.setPluginId(layer.getPluginId());
        return response;
    }
}
