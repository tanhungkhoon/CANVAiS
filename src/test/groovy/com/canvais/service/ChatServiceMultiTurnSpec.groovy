package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.BoundingBoxDto
import com.canvais.api.dto.request.SendChatMessageRequest
import com.canvais.api.dto.response.ProposedEntityDto
import com.canvais.domain.model.ChatActionType
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
 * Specification testing multi-turn conversational modifications and deletions of sketched entities.
 */
class ChatServiceMultiTurnSpec extends SpecificationBase {

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

    def "sendMessage handles UPDATE action modifying existing entity style in-place"() {
        given: "an existing entity on the overlay layer"
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
                name: "Command Perimeter",
                entityType: "PerimeterZone",
                geometryType: GeometryType.POLYGON,
                geometryGeoJson: '{"type":"Polygon","coordinates":[[[8.18,48.17],[8.22,48.17],[8.22,48.19],[8.18,48.19],[8.18,48.17]]]}',
                style: new EntityStyle("#E76F51", "#E76F5133", 2.5f, null, null)
        )
        graphicalEntityRepository.findByLayerId(overlayLayerId) >> [existingEntity]
        graphicalEntityRepository.findById(entityId) >> Optional.of(existingEntity)

        def updatedStyle = new EntityStyle("#FF0000", "#FF000033", 3.0f, null, null)
        def updateDef = new ProposedEntityDto(
                name: "High Security Zone",
                entityType: "PerimeterZone",
                style: updatedStyle
        )
        def aiResult = new AiSpatialResult("Style updated to red", ChatActionType.UPDATE, entityId, [updateDef])
        aiModelClientAdapter.generateSpatialIntent(*_) >> aiResult

        def request = new SendChatMessageRequest("Change line color to red and rename to High Security Zone", null, null, null)

        when: "sendMessage is called"
        def turnResponse = chatService.sendMessage(chatSessionId, request)

        then: "target entity is updated and saved without duplicating"
        1 * graphicalEntityRepository.save({ GraphicalEntity ent ->
            ent.id == entityId && ent.name == "High Security Zone" && ent.style.strokeColor == "#FF0000"
        })
        turnResponse != null
        turnResponse.actionType == ChatActionType.UPDATE
        turnResponse.affectedEntities.size() == 1
        turnResponse.affectedEntities[0].name == "High Security Zone"
    }

    def "sendMessage handles DELETE action removing target entity from overlay layer"() {
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
                name: "Temporary Marker",
                entityType: "ObservationPoint"
        )
        graphicalEntityRepository.findByLayerId(overlayLayerId) >> [existingEntity]
        graphicalEntityRepository.findById(entityId) >> Optional.of(existingEntity)

        def aiResult = new AiSpatialResult("Marker removed", ChatActionType.DELETE, entityId, [])
        aiModelClientAdapter.generateSpatialIntent(_ as String, _ as List, _ as List, null, null) >> aiResult

        def request = new SendChatMessageRequest("Remove the temporary marker", null, null, null)

        when: "sendMessage is called"
        def turnResponse = chatService.sendMessage(chatSessionId, request)

        then: "entity is deleted from the repository"
        1 * graphicalEntityRepository.delete({ GraphicalEntity ent -> ent.id == entityId })
        turnResponse != null
        turnResponse.actionType == ChatActionType.DELETE
        turnResponse.affectedEntities.size() == 1
        turnResponse.affectedEntities[0].id == entityId
    }
}
