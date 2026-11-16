package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.SendChatMessageRequest
import com.canvais.api.dto.response.ProposedEntityDto
import com.canvais.domain.model.ChatActionType
import com.canvais.domain.model.ChatEntityAction
import com.canvais.domain.model.ChatMessage
import com.canvais.domain.model.ChatRole
import com.canvais.domain.model.ChatSession
import com.canvais.domain.model.EntityStyle
import com.canvais.domain.model.GeometryType
import com.canvais.domain.model.GraphicalEntity
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
 * Specification verifying ChatEntityAction audit logging during entity modifications and deletions.
 */
class ChatEntityActionSpec extends SpecificationBase {

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

    def "sendMessage records previousStateJson snapshot in ChatEntityAction during update"() {
        given: "an entity on the overlay layer"
        def chatSessionId = UUID.randomUUID()
        def mapSessionId = UUID.randomUUID()
        def overlayLayerId = UUID.randomUUID()
        def entityId = UUID.randomUUID()

        def chatSession = new ChatSession(chatSessionId, mapSessionId, overlayLayerId, "Zone Chat", "llama3.2")
        chatSessionRepository.findById(chatSessionId) >> Optional.of(chatSession)
        chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(chatSessionId) >> []

        def existingEntity = new GraphicalEntity(
                id: entityId,
                layerId: overlayLayerId,
                name: "Old Perimeter",
                entityType: "PerimeterZone",
                geometryType: GeometryType.POLYGON,
                geometryGeoJson: '{"type":"Polygon","coordinates":[[[8.18,48.17],[8.22,48.17],[8.22,48.19],[8.18,48.19],[8.18,48.17]]]}',
                style: new EntityStyle("#E76F51", null, 2.5f, null, null)
        )
        graphicalEntityRepository.findByLayerId(overlayLayerId) >> [existingEntity]
        graphicalEntityRepository.findById(entityId) >> Optional.of(existingEntity)

        def updateDef = new ProposedEntityDto(name: "New Perimeter")
        def aiResult = new AiSpatialResult("Renamed", ChatActionType.UPDATE, entityId, [updateDef])
        aiModelClientAdapter.generateSpatialIntent(_ as String, _ as List, _ as List, null, null) >> aiResult

        when: "sendMessage is called"
        chatService.sendMessage(chatSessionId, new SendChatMessageRequest("Rename to New Perimeter", null, null, null))

        then: "ChatEntityAction is saved with previousStateJson containing the old name"
        1 * chatEntityActionRepository.save({ ChatEntityAction action ->
            action.entityId == entityId &&
            action.actionType == ChatActionType.UPDATE &&
            action.previousStateJson != null &&
            action.previousStateJson.contains("Old Perimeter")
        })
    }
}
