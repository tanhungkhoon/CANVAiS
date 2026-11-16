package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.domain.model.ChatSession
import com.canvais.domain.model.GraphicalEntity
import com.canvais.domain.model.LayerType
import com.canvais.domain.model.MapLayer
import com.canvais.domain.repository.ChatEntityActionRepository
import com.canvais.domain.repository.ChatMessageRepository
import com.canvais.domain.repository.ChatSessionRepository
import com.canvais.domain.repository.GraphicalEntityRepository
import com.canvais.domain.repository.MapLayerRepository
import com.canvais.domain.repository.MapSessionRepository
import com.canvais.infrastructure.ai.AiModelClientAdapter
import com.canvais.infrastructure.geospatial.JtsGeometryAdapter

/**
 * Specification verifying overlay layer isolation and clearing operations.
 */
class ChatOverlayLayerSpec extends SpecificationBase {

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

    def "clearOverlayLayer removes all entities on the overlay layer while keeping layer metadata"() {
        given: "a chat session targeting an overlay layer with 3 entities"
        def chatSessionId = UUID.randomUUID()
        def mapSessionId = UUID.randomUUID()
        def overlayLayerId = UUID.randomUUID()
        def chatSession = new ChatSession(chatSessionId, mapSessionId, overlayLayerId, "Zone Chat", "llama3.2")
        chatSessionRepository.findById(chatSessionId) >> Optional.of(chatSession)

        def entity1 = new GraphicalEntity(id: UUID.randomUUID(), layerId: overlayLayerId)
        def entity2 = new GraphicalEntity(id: UUID.randomUUID(), layerId: overlayLayerId)
        def entity3 = new GraphicalEntity(id: UUID.randomUUID(), layerId: overlayLayerId)
        graphicalEntityRepository.findByLayerId(overlayLayerId) >> [entity1, entity2, entity3]

        when: "clearOverlayLayer is invoked"
        def response = chatService.clearOverlayLayer(chatSessionId)

        then: "all 3 entities are deleted from repository"
        1 * graphicalEntityRepository.deleteAll([entity1, entity2, entity3])
        0 * mapLayerRepository.deleteById(overlayLayerId)
        response != null
        response.layerId == overlayLayerId
        response.clearedEntitiesCount == 3
    }
}
