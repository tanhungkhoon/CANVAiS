package com.canvais.api.controller;

import com.canvais.api.dto.request.CreateLayerRequest;
import com.canvais.api.dto.request.CreateSessionRequest;
import com.canvais.api.dto.response.MapLayerResponse;
import com.canvais.api.dto.response.MapSessionDetailResponse;
import com.canvais.api.dto.response.MapSessionResponse;
import com.canvais.service.LayerService;
import com.canvais.service.MapSessionService;
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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for MapSession management and layer additions.
 */
@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Map Sessions", description = "Endpoints for managing collaborative map sketching sessions")
public class MapSessionController {

    private final MapSessionService sessionService;
    private final LayerService layerService;

    public MapSessionController(MapSessionService sessionService, LayerService layerService) {
        this.sessionService = sessionService;
        this.layerService = layerService;
    }

    /**
     * Creates a new MapSession.
     *
     * @param request session parameters
     * @return 201 Created with session details
     */
    @PostMapping
    @Operation(summary = "Create a new map session")
    public ResponseEntity<MapSessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        MapSessionResponse created = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Lists all existing map sessions.
     *
     * @return 200 OK with list of sessions
     */
    @GetMapping
    @Operation(summary = "List all map sessions")
    public ResponseEntity<List<MapSessionResponse>> listSessions() {
        return ResponseEntity.ok(sessionService.listSessions());
    }

    /**
     * Retrieves detailed session information with all attached layers.
     *
     * @param sessionId session UUID
     * @return 200 OK with session detail
     */
    @GetMapping("/{sessionId}")
    @Operation(summary = "Get map session details by ID")
    public ResponseEntity<MapSessionDetailResponse> getSession(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(sessionService.getSessionDetail(sessionId));
    }

    /**
     * Adds an organizational layer to a map session.
     *
     * @param sessionId session UUID
     * @param request layer creation request
     * @return 201 Created with layer details
     */
    @PostMapping("/{sessionId}/layers")
    @Operation(summary = "Add a layer to a session")
    public ResponseEntity<MapLayerResponse> createLayer(@PathVariable UUID sessionId,
                                                        @Valid @RequestBody CreateLayerRequest request) {
        MapLayerResponse created = layerService.createLayer(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
