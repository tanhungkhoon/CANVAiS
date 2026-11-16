package com.canvais.service;

import com.canvais.api.dto.request.CreateChatSessionRequest;
import com.canvais.api.dto.request.GesturePointDto;
import com.canvais.api.dto.request.IngestVibeToChatRequest;
import com.canvais.api.dto.request.SendChatMessageRequest;
import com.canvais.api.dto.response.AiHealthResponse;
import com.canvais.api.dto.response.ChatMessageDto;
import com.canvais.api.dto.response.ChatMessageTurnResponse;
import com.canvais.api.dto.response.ChatSessionDetailResponse;
import com.canvais.api.dto.response.ChatSessionResponse;
import com.canvais.api.dto.response.ClearLayerResponse;
import com.canvais.api.dto.response.GraphicalEntityResponse;
import com.canvais.api.dto.response.ProposedEntityDto;
import com.canvais.domain.model.ChatActionType;
import com.canvais.domain.model.ChatEntityAction;
import com.canvais.domain.model.ChatMessage;
import com.canvais.domain.model.ChatRole;
import com.canvais.domain.model.ChatSession;
import com.canvais.domain.model.GeometryType;
import com.canvais.domain.model.GraphicalEntity;
import com.canvais.domain.model.LayerType;
import com.canvais.domain.model.MapLayer;
import com.canvais.domain.model.ProvenanceType;
import com.canvais.domain.repository.ChatEntityActionRepository;
import com.canvais.domain.repository.ChatMessageRepository;
import com.canvais.domain.repository.ChatSessionRepository;
import com.canvais.domain.repository.GraphicalEntityRepository;
import com.canvais.domain.repository.MapLayerRepository;
import com.canvais.domain.repository.MapSessionRepository;
import com.canvais.infrastructure.ai.AiModelClientAdapter;
import com.canvais.infrastructure.ai.AiSpatialResult;
import com.canvais.infrastructure.geospatial.JtsGeometryAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core application service coordinating chat sessions, multi-turn AI sketching,
 * dedicated overlay layer management, and vibe record ingestion.
 */
@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatEntityActionRepository chatEntityActionRepository;
    private final MapSessionRepository mapSessionRepository;
    private final MapLayerRepository mapLayerRepository;
    private final GraphicalEntityRepository graphicalEntityRepository;
    private final AiModelClientAdapter aiModelClientAdapter;
    private final JtsGeometryAdapter jtsGeometryAdapter;

    public ChatService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            ChatEntityActionRepository chatEntityActionRepository,
            MapSessionRepository mapSessionRepository,
            MapLayerRepository mapLayerRepository,
            GraphicalEntityRepository graphicalEntityRepository,
            AiModelClientAdapter aiModelClientAdapter,
            JtsGeometryAdapter jtsGeometryAdapter) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatEntityActionRepository = chatEntityActionRepository;
        this.mapSessionRepository = mapSessionRepository;
        this.mapLayerRepository = mapLayerRepository;
        this.graphicalEntityRepository = graphicalEntityRepository;
        this.aiModelClientAdapter = aiModelClientAdapter;
        this.jtsGeometryAdapter = jtsGeometryAdapter;
    }

    /**
     * Initializes or retrieves an active chat session for a map session, auto-provisioning
     * a dedicated overlay layer above the base map layer if one does not exist.
     *
     * @param request session creation parameters
     * @return initialized chat session response
     */
    public ChatSessionResponse getOrCreateSession(CreateChatSessionRequest request) {
        UUID mapSessionId = request.getMapSessionId();
        if (!mapSessionRepository.existsById(mapSessionId)) {
            throw new NoSuchElementException("MapSession not found with ID: " + mapSessionId);
        }

        // Check if an existing chat session is active with a valid target layer
        Optional<ChatSession> existingSessionOpt = chatSessionRepository
                .findFirstByMapSessionIdOrderByCreatedAtDesc(mapSessionId);

        if (existingSessionOpt.isPresent()) {
            ChatSession session = existingSessionOpt.get();
            if (mapLayerRepository.existsById(session.getTargetLayerId())) {
                return ChatSessionResponse.fromEntity(session);
            }
        }

        // Provision or locate dedicated OVERLAY layer
        List<MapLayer> layers = mapLayerRepository.findBySessionIdOrderByOrderIndexAsc(mapSessionId);
        MapLayer overlayLayer = layers.stream()
                .filter(l -> l.getLayerType() == LayerType.OVERLAY)
                .findFirst()
                .orElseGet(() -> {
                    int nextOrder = layers.stream().mapToInt(MapLayer::getOrderIndex).max().orElse(0) + 10;
                    MapLayer newOverlay = new MapLayer();
                    newOverlay.setSessionId(mapSessionId);
                    newOverlay.setName("Chat Sketch Overlay");
                    newOverlay.setLayerType(LayerType.OVERLAY);
                    newOverlay.setOrderIndex(Math.max(100, nextOrder));
                    newOverlay.setVisible(true);
                    newOverlay.setOpacity(1.0);
                    return mapLayerRepository.save(newOverlay);
                });

        String title = request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle()
                : "Chat Sketching Session";

        ChatSession chatSession = new ChatSession(
                UUID.randomUUID(),
                mapSessionId,
                overlayLayer.getId(),
                title,
                "llama3.2"
        );

        ChatSession saved = chatSessionRepository.save(chatSession);
        return ChatSessionResponse.fromEntity(saved);
    }

    /**
     * Retrieves full chat session details including chronological message history.
     *
     * @param chatSessionId unique identifier of the chat session
     * @return chat session details and message turns
     */
    @Transactional(readOnly = true)
    public ChatSessionDetailResponse getSessionDetail(UUID chatSessionId) {
        ChatSession session = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new NoSuchElementException("ChatSession not found with ID: " + chatSessionId));

        List<ChatMessageDto> messages = chatMessageRepository
                .findByChatSessionIdOrderByTimestampAsc(chatSessionId).stream()
                .map(ChatMessageDto::fromEntity)
                .collect(Collectors.toList());

        return new ChatSessionDetailResponse(ChatSessionResponse.fromEntity(session), messages);
    }

    /**
     * Executes a user conversational sketching prompt, calls the local AI model,
     * updates the overlay layer accordingly, and logs the message turn and entity actions.
     *
     * @param chatSessionId target chat session
     * @param request user prompt and spatial context
     * @return turn response with generated/modified entities
     */
    public ChatMessageTurnResponse sendMessage(UUID chatSessionId, SendChatMessageRequest request) {
        ChatSession session = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new NoSuchElementException("ChatSession not found with ID: " + chatSessionId));

        // 1. Record user message turn
        ChatMessage userMsg = new ChatMessage(
                UUID.randomUUID(),
                chatSessionId,
                ChatRole.USER,
                request.getMessage(),
                request.getVibeRecordId(),
                ChatActionType.NONE,
                null
        );
        chatMessageRepository.save(userMsg);

        // 2. Fetch context
        List<ChatMessage> history = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(chatSessionId);
        List<GraphicalEntity> existingEntities = graphicalEntityRepository.findByLayerId(session.getTargetLayerId());

        // 3. Delegate to local AI model adapter
        AiSpatialResult aiResult = aiModelClientAdapter.generateSpatialIntent(
                request.getMessage(),
                history,
                existingEntities,
                request.getViewportBounds(),
                request.getCenterCoordinates()
        );
        if (aiResult == null) {
            aiResult = new AiSpatialResult("No response from AI model.", ChatActionType.NONE, null, new ArrayList<>());
        }

        // 4. Process actions on the overlay layer
        List<GraphicalEntity> affectedEntities = new ArrayList<>();
        ChatActionType actionType = aiResult.getActionType();

        if (actionType == ChatActionType.CREATE && !aiResult.getEntities().isEmpty()) {
            for (ProposedEntityDto proposed : aiResult.getEntities()) {
                GraphicalEntity entity = new GraphicalEntity();
                entity.setId(UUID.randomUUID());
                entity.setLayerId(session.getTargetLayerId());
                entity.setName(proposed.getName());
                entity.setEntityType(proposed.getEntityType());
                entity.setStyle(proposed.getStyle());
                entity.setSemanticAttributes(proposed.getSemanticAttributes());
                entity.setConfidenceScore(proposed.getConfidence());
                entity.setProvenance(ProvenanceType.VIBE_AI);

                // Convert geometry
                try {
                    Geometry jtsGeom = jtsGeometryAdapter.parseGeoJson(proposed.getGeometry());
                    entity.setGeometryGeoJson(OBJECT_MAPPER.writeValueAsString(proposed.getGeometry()));
                    entity.setGeometryType(mapGeometryType(jtsGeom.getGeometryType()));
                    affectedEntities.add(entity);
                } catch (Exception ex) {
                    log.warn("Failed to parse geometry for proposed entity '{}': {}", proposed.getName(), ex.getMessage());
                }
            }

            if (!affectedEntities.isEmpty()) {
                graphicalEntityRepository.saveAll(affectedEntities);
                for (GraphicalEntity ent : affectedEntities) {
                    ChatEntityAction action = new ChatEntityAction(
                            UUID.randomUUID(),
                            userMsg.getId(),
                            ent.getId(),
                            ChatActionType.CREATE,
                            null
                    );
                    chatEntityActionRepository.save(action);
                }
            }
        } else if (actionType == ChatActionType.UPDATE && aiResult.getTargetEntityId() != null) {
            UUID targetId = aiResult.getTargetEntityId();
            Optional<GraphicalEntity> targetOpt = graphicalEntityRepository.findById(targetId);
            if (targetOpt.isPresent() && targetOpt.get().getLayerId().equals(session.getTargetLayerId())) {
                GraphicalEntity target = targetOpt.get();
                String previousState = serializeEntitySafe(target);

                if (!aiResult.getEntities().isEmpty()) {
                    ProposedEntityDto updateDef = aiResult.getEntities().get(0);
                    if (updateDef.getName() != null) {
                        target.setName(updateDef.getName());
                    }
                    if (updateDef.getStyle() != null) {
                        target.setStyle(updateDef.getStyle());
                    }
                    if (updateDef.getGeometry() != null) {
                        try {
                            Geometry jtsGeom = jtsGeometryAdapter.parseGeoJson(updateDef.getGeometry());
                            target.setGeometryGeoJson(OBJECT_MAPPER.writeValueAsString(updateDef.getGeometry()));
                            target.setGeometryType(mapGeometryType(jtsGeom.getGeometryType()));
                        } catch (Exception ignored) {
                        }
                    }
                }
                graphicalEntityRepository.save(target);
                affectedEntities.add(target);

                ChatEntityAction action = new ChatEntityAction(
                        UUID.randomUUID(),
                        userMsg.getId(),
                        target.getId(),
                        ChatActionType.UPDATE,
                        previousState
                );
                chatEntityActionRepository.save(action);
            }
        } else if (actionType == ChatActionType.DELETE && aiResult.getTargetEntityId() != null) {
            UUID targetId = aiResult.getTargetEntityId();
            Optional<GraphicalEntity> targetOpt = graphicalEntityRepository.findById(targetId);
            if (targetOpt.isPresent() && targetOpt.get().getLayerId().equals(session.getTargetLayerId())) {
                GraphicalEntity target = targetOpt.get();
                String previousState = serializeEntitySafe(target);
                graphicalEntityRepository.delete(target);
                affectedEntities.add(target);

                ChatEntityAction action = new ChatEntityAction(
                        UUID.randomUUID(),
                        userMsg.getId(),
                        target.getId(),
                        ChatActionType.DELETE,
                        previousState
                );
                chatEntityActionRepository.save(action);
            }
        }

        // 5. Record assistant response turn
        ChatMessage assistantMsg = new ChatMessage(
                UUID.randomUUID(),
                chatSessionId,
                ChatRole.ASSISTANT,
                aiResult.getAssistantMessage(),
                null,
                actionType,
                null
        );
        chatMessageRepository.save(assistantMsg);

        // Update session timestamp
        session.setUpdatedAt(Instant.now());
        chatSessionRepository.save(session);

        // Map response
        List<GraphicalEntityResponse> entityResponses = affectedEntities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new ChatMessageTurnResponse(
                ChatMessageDto.fromEntity(userMsg),
                ChatMessageDto.fromEntity(assistantMsg),
                actionType,
                session.getTargetLayerId(),
                entityResponses
        );
    }

    /**
     * Ingests a captured vibe transcript and gesture points directly into the chat stream.
     *
     * @param chatSessionId target chat session
     * @param request vibe ingestion request
     * @return chat message turn response
     */
    public ChatMessageTurnResponse ingestVibe(UUID chatSessionId, IngestVibeToChatRequest request) {
        GesturePointDto center = null;
        if (request.getGesturePoints() != null && !request.getGesturePoints().isEmpty()) {
            center = request.getGesturePoints().get(0);
        }

        SendChatMessageRequest messageReq = new SendChatMessageRequest(
                request.getVibeTranscript(),
                request.getVibeRecordId(),
                request.getViewportBounds(),
                center
        );

        return sendMessage(chatSessionId, messageReq);
    }

    /**
     * Clears all sketched objects residing on the chat overlay layer without modifying
     * other domain layers or base map tiles.
     *
     * @param chatSessionId target chat session
     * @return summary response with count of cleared entities
     */
    public ClearLayerResponse clearOverlayLayer(UUID chatSessionId) {
        ChatSession session = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new NoSuchElementException("ChatSession not found with ID: " + chatSessionId));

        List<GraphicalEntity> entities = graphicalEntityRepository.findByLayerId(session.getTargetLayerId());
        int count = entities.size();
        if (count > 0) {
            graphicalEntityRepository.deleteAll(entities);
        }
        return new ClearLayerResponse(session.getTargetLayerId(), count);
    }

    /**
     * Probes connectivity and operational readiness of the local AI provider.
     *
     * @return health report
     */
    public AiHealthResponse checkHealth() {
        return aiModelClientAdapter.checkHealth();
    }

    private GraphicalEntityResponse toResponse(GraphicalEntity entity) {
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

    private String serializeEntitySafe(GraphicalEntity entity) {
        try {
            return OBJECT_MAPPER.writeValueAsString(toResponse(entity));
        } catch (Exception ex) {
            return null;
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
