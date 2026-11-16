package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.BoundingBoxDto
import com.canvais.api.dto.request.CreateChatSessionRequest
import com.canvais.api.dto.request.GesturePointDto
import com.canvais.api.dto.request.IngestVibeToChatRequest
import com.canvais.api.dto.request.SendChatMessageRequest
import com.canvais.api.dto.response.ProposedEntityDto
import com.canvais.domain.model.ChatActionType
import com.canvais.domain.model.ChatMessage
import com.canvais.domain.model.ChatRole
import com.canvais.domain.model.ChatSession
import com.canvais.domain.model.EntityStyle
import com.canvais.domain.model.LayerType
import com.canvais.domain.model.MapLayer
import com.canvais.domain.model.MapSession
import com.canvais.domain.repository.ChatEntityActionRepository
import com.canvais.domain.repository.ChatMessageRepository
import com.canvais.domain.repository.ChatSessionRepository
import com.canvais.domain.repository.GraphicalEntityRepository
import com.canvais.domain.repository.MapLayerRepository
import com.canvais.domain.repository.MapSessionRepository
import com.canvais.infrastructure.ai.AiModelClientAdapter
import com.canvais.infrastructure.ai.AiSpatialResult
import com.canvais.infrastructure.geospatial.JtsGeometryAdapter

/**
 * Specification verifying ChatService business logic for session creation, overlay layer provisioning,
 * and conversational entity sketching.
 */
class ChatServiceSpec extends SpecificationBase {

    ChatSessionRepository chatSessionRepository = Mock()
    ChatMessageRepository chatMessageRepository = Mock()
    ChatEntityActionRepository chatEntityActionRepository = Mock()
    MapSessionRepository mapSessionRepository = Mock()
    MapLayerRepository mapLayerRepository = Mock()
    GraphicalEntityRepository graphicalEntityRepository = Mock()
    AiModelClientAdapter aiModelClientAdapter = Mock()
    JtsGeometryAdapter jtsGeometryAdapter = new JtsGeometryAdapter()

    ChatService chatService

    def setup() {
        chatService = new ChatService(
                chatSessionRepository,
                chatMessageRepository,
                chatEntityActionRepository,
                mapSessionRepository,
                mapLayerRepository,
                graphicalEntityRepository,
                aiModelClientAdapter,
                jtsGeometryAdapter
        )
    }

    def "getOrCreateSession creates new overlay layer if none exists"() {
        given: "an existing map session without chat overlay layer"
        def mapSessionId = UUID.randomUUID()
        def mapSession = new MapSession(id: mapSessionId, title: "Tactical Map", centerLat: 48.18, centerLng: 8.20, zoomLevel: 12)
        mapSessionRepository.existsById(mapSessionId) >> true
        mapSessionRepository.findById(mapSessionId) >> Optional.of(mapSession)
        chatSessionRepository.findFirstByMapSessionIdOrderByCreatedAtDesc(mapSessionId) >> Optional.empty()
        mapLayerRepository.findBySessionIdOrderByOrderIndexAsc(mapSessionId) >> []

        when: "getOrCreateSession is called"
        def response = chatService.getOrCreateSession(new CreateChatSessionRequest(mapSessionId, "Tactical Chat"))

        then: "a new overlay layer and chat session are persisted"
        1 * mapLayerRepository.save({ MapLayer layer ->
            layer.sessionId == mapSessionId && layer.layerType == LayerType.OVERLAY && layer.name == "Chat Sketch Overlay"
        }) >> { MapLayer layer -> layer.id = UUID.randomUUID(); layer }
        1 * chatSessionRepository.save(_ as ChatSession) >> { ChatSession session -> session.id = UUID.randomUUID(); session }
        response != null
        response.mapSessionId == mapSessionId
        response.title == "Tactical Chat"
    }

    def "sendMessage processes prompt and persists generated entities on overlay layer"() {
        given: "an active chat session and overlay layer"
        def chatSessionId = UUID.randomUUID()
        def mapSessionId = UUID.randomUUID()
        def overlayLayerId = UUID.randomUUID()
        def chatSession = new ChatSession(chatSessionId, mapSessionId, overlayLayerId, "Zone Chat", "llama3.2")
        chatSessionRepository.findById(chatSessionId) >> Optional.of(chatSession)
        chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(chatSessionId) >> []
        graphicalEntityRepository.findByLayerId(overlayLayerId) >> []

        def proposed = new ProposedEntityDto(
                name: "Command Perimeter",
                entityType: "PerimeterZone",
                geometry: [type: "Polygon", coordinates: [[[8.18, 48.17], [8.22, 48.17], [8.22, 48.19], [8.18, 48.19], [8.18, 48.17]]]],
                style: new EntityStyle("#E76F51", "#E76F5133", 2.5f, null, null),
                confidence: 0.95
        )
        def aiResult = new AiSpatialResult("Perimeter created", ChatActionType.CREATE, null, [proposed])
        aiModelClientAdapter.generateSpatialIntent(_ as String, _ as List, _ as List, _ as BoundingBoxDto, _ as GesturePointDto) >> aiResult

        def request = new SendChatMessageRequest(
                message: "Draw perimeter",
                vibeRecordId: null,
                viewportBounds: new BoundingBoxDto(48.20, 48.16, 8.23, 8.17),
                centerCoordinates: new GesturePointDto(48.18, 8.20)
        )

        when: "sendMessage is called"
        def turnResponse = chatService.sendMessage(chatSessionId, request)

        then: "entities and messages are saved and returned"
        1 * graphicalEntityRepository.saveAll(_ as List) >> { List entities -> entities }
        2 * chatMessageRepository.save(_ as ChatMessage) >> { ChatMessage m -> m.id = UUID.randomUUID(); m }
        turnResponse != null
        turnResponse.actionType == ChatActionType.CREATE
        turnResponse.targetLayerId == overlayLayerId
        turnResponse.affectedEntities.size() == 1
        turnResponse.affectedEntities[0].name == "Command Perimeter"
    }
}
