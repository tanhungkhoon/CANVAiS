package com.canvais.service;

import com.canvais.api.dto.request.CreateSessionRequest;
import com.canvais.api.dto.response.MapSessionDetailResponse;
import com.canvais.api.dto.response.MapSessionResponse;
import com.canvais.domain.model.MapSession;
import com.canvais.domain.repository.MapSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing MapSession lifecycle and queries.
 */
@Service
@Transactional
public class MapSessionService {

    private final MapSessionRepository sessionRepository;
    private final LayerService layerService;

    public MapSessionService(MapSessionRepository sessionRepository, LayerService layerService) {
        this.sessionRepository = sessionRepository;
        this.layerService = layerService;
    }

    /**
     * Creates a new MapSession from the request.
     *
     * @param request creation parameters
     * @return summary response DTO
     */
    public MapSessionResponse createSession(CreateSessionRequest request) {
        MapSession session = new MapSession(
                request.getTitle(),
                request.getDescription(),
                request.getCenterLat(),
                request.getCenterLng(),
                request.getZoomLevel()
        );
        MapSession saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    /**
     * Retrieves all map sessions.
     *
     * @return list of session responses
     */
    @Transactional(readOnly = true)
    public List<MapSessionResponse> listSessions() {
        return sessionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a detailed session including all attached layers.
     *
     * @param sessionId session UUID
     * @return detailed response DTO
     */
    @Transactional(readOnly = true)
    public MapSessionDetailResponse getSessionDetail(UUID sessionId) {
        MapSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("MapSession not found with ID: " + sessionId));

        MapSessionDetailResponse response = new MapSessionDetailResponse();
        populateBaseResponse(session, response);
        response.setLayers(layerService.listLayersBySession(sessionId));
        return response;
    }

    /**
     * Helper mapping entity to summary response.
     */
    public MapSessionResponse toResponse(MapSession session) {
        MapSessionResponse response = new MapSessionResponse();
        populateBaseResponse(session, response);
        return response;
    }

    private void populateBaseResponse(MapSession session, MapSessionResponse response) {
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setDescription(session.getDescription());
        response.setCenterLat(session.getCenterLat());
        response.setCenterLng(session.getCenterLng());
        response.setZoomLevel(session.getZoomLevel());
        response.setCrs(session.getCrs());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
    }
}
