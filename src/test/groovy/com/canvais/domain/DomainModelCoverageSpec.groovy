package com.canvais.domain

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.BoundingBoxDto
import com.canvais.api.dto.request.CreateEntityRequest
import com.canvais.api.dto.request.CreateLayerRequest
import com.canvais.api.dto.request.CreateSemanticSchemaRequest
import com.canvais.api.dto.request.CreateSessionRequest
import com.canvais.api.dto.request.GesturePointDto
import com.canvais.api.dto.request.RegisterMcpServerRequest
import com.canvais.api.dto.request.VibeIntentProposeRequest
import com.canvais.api.dto.response.GraphicalEntityResponse
import com.canvais.api.dto.response.MapLayerResponse
import com.canvais.api.dto.response.MapSessionDetailResponse
import com.canvais.api.dto.response.MapSessionResponse
import com.canvais.api.dto.response.McpDiscoveryResponse
import com.canvais.api.dto.response.McpServerStatusResponse
import com.canvais.api.dto.response.ProposedEntityDto
import com.canvais.api.dto.response.SemanticSchemaResponse
import com.canvais.api.dto.response.VibeIntentProposalResponse
import com.canvais.domain.model.AttributeDefinition
import com.canvais.domain.model.ChatActionType
import com.canvais.domain.model.ChatEntityAction
import com.canvais.domain.model.ChatMessage
import com.canvais.domain.model.ChatRole
import com.canvais.domain.model.ChatSession
import com.canvais.domain.model.ConnectionStatus
import com.canvais.domain.model.DataType
import com.canvais.domain.model.EntityStyle
import com.canvais.domain.model.EntityTypeDefinition
import com.canvais.domain.model.GeometryType
import com.canvais.domain.model.GraphicalEntity
import com.canvais.domain.model.LayerType
import com.canvais.domain.model.MapLayer
import com.canvais.domain.model.MapSession
import com.canvais.domain.model.McpServerConfig
import com.canvais.domain.model.ProposalStatus
import com.canvais.domain.model.ProvenanceType
import com.canvais.domain.model.SemanticSchema
import com.canvais.domain.model.TransportType
import com.canvais.domain.model.VibeIntentProposal

import java.time.Instant

/**
 * Specification verifying complete domain model, DTO, and value object behaviors.
 */
class DomainModelCoverageSpec extends SpecificationBase {

    def "verify MapSession model lifecycle and properties"() {
        when:
        def session = new MapSession("Test Session", "Description", 48.0, 8.0, 10)
        session.onCreate()
        session.onUpdate()
        session.setLayers([])

        then:
        session.getId() != null
        session.getTitle() == "Test Session"
        session.getDescription() == "Description"
        session.getCenterLat() == 48.0
        session.getCenterLng() == 8.0
        session.getZoomLevel() == 10
        session.getCrs() == "EPSG:4326"
        session.getCreatedAt() != null
        session.getUpdatedAt() != null
        session.getLayers().isEmpty()
    }

    def "verify MapLayer model properties"() {
        when:
        def layer = new MapLayer(UUID.randomUUID(), "Tactical Layer", 1, LayerType.CUSTOM_SEMANTIC)
        layer.onCreate()
        layer.setEntities([])
        layer.setSchemaId(UUID.randomUUID())
        layer.setPluginId("plugin-1")

        then:
        layer.getId() != null
        layer.getName() == "Tactical Layer"
        layer.getOrderIndex() == 1
        layer.getVisible() == true
        layer.getOpacity() == 1.0
        layer.getLayerType() == LayerType.CUSTOM_SEMANTIC
        layer.getSchemaId() != null
        layer.getPluginId() == "plugin-1"
        layer.getEntities().isEmpty()
    }

    def "verify GraphicalEntity model properties"() {
        when:
        def entity = new GraphicalEntity(UUID.randomUUID(), "ObservationPost", GeometryType.POINT, '{"type":"Point","coordinates":[8.2,48.1]}')
        entity.onCreate()
        entity.setName("Post 1")
        entity.setSemanticAttributes([status: "ACTIVE"])
        entity.setConfidenceScore(0.95)
        entity.setProvenance(ProvenanceType.VIBE_AI)

        then:
        entity.getId() != null
        entity.getName() == "Post 1"
        entity.getEntityType() == "ObservationPost"
        entity.getGeometryType() == GeometryType.POINT
        entity.getSemanticAttributes().status == "ACTIVE"
        entity.getConfidenceScore() == 0.95
        entity.getProvenance() == ProvenanceType.VIBE_AI
    }

    def "verify EntityStyle properties"() {
        when:
        def style = new EntityStyle("#111111", "#222222", 3.5f, "2, 2", "flag")

        then:
        style.getStrokeColor() == "#111111"
        style.getFillColor() == "#222222"
        style.getStrokeWidth() == 3.5f
        style.getDashArray() == "2, 2"
        style.getIcon() == "flag"
    }

    def "verify VibeIntentProposal model properties"() {
        when:
        def proposal = new VibeIntentProposal(UUID.randomUUID(), "Summary")
        proposal.onCreate()
        proposal.setProposedEntities([new ProposedEntityDto()])
        proposal.setStatus(ProposalStatus.REFINED)

        then:
        proposal.getProposalId() != null
        proposal.getInterpretedSummary() == "Summary"
        proposal.getProposedEntities().size() == 1
        proposal.getStatus() == ProposalStatus.REFINED
        proposal.getCreatedAt() != null
    }

    def "verify McpServerConfig model properties"() {
        when:
        def mcp = new McpServerConfig("mcp-1", "MCP One", TransportType.SSE, "http://localhost:8081/mcp/sse")
        mcp.onCreate()
        mcp.setDiscoveredTools(["tool1", "tool2"])
        mcp.setAssociatedLayerIds([UUID.randomUUID()])
        mcp.setConnectionStatus(ConnectionStatus.CONNECTED)
        mcp.setLastDiscoveredAt(Instant.now())

        then:
        mcp.getId() == "mcp-1"
        mcp.getName() == "MCP One"
        mcp.getTransportType() == TransportType.SSE
        mcp.getConnectionStatus() == ConnectionStatus.CONNECTED
        mcp.getDiscoveredTools().size() == 2
        mcp.getAssociatedLayerIds().size() == 1
        mcp.getLastDiscoveredAt() != null
    }

    def "verify SemanticSchema model properties"() {
        when:
        def schema = new SemanticSchema("TestSchema", "TestCategory", "1.0.0")
        schema.onCreate()
        def attr = new AttributeDefinition("attr1", DataType.NUMBER, true, [])
        def typeDef = new EntityTypeDefinition("type1", "Type 1", [GeometryType.POLYGON])
        typeDef.setAttributes([attr])
        schema.setEntityTypes([typeDef])

        then:
        schema.getId() != null
        schema.getName() == "TestSchema"
        schema.getDomainCategory() == "TestCategory"
        schema.getVersion() == "1.0.0"
        schema.getEntityTypes().size() == 1
        schema.getEntityTypes()[0].getAttributes().size() == 1
    }

    def "verify DTO getters and setters"() {
        expect:
        new BoundingBoxDto(49.0, 47.0, 9.0, 7.0).getNorth() == 49.0
        new GesturePointDto(48.0, 8.0, 100L).getLat() == 48.0
        new CreateSessionRequest().getTitle() == null
        new CreateLayerRequest().getName() == null
        new CreateEntityRequest().getName() == null
        new CreateSemanticSchemaRequest().getName() == null
        new RegisterMcpServerRequest().getName() == null
        new VibeIntentProposeRequest().getTextVibe() == null
        new MapSessionResponse().getTitle() == null
        new MapSessionDetailResponse().getLayers().isEmpty()
        new MapLayerResponse().getName() == null
        new GraphicalEntityResponse().getName() == null
        new McpDiscoveryResponse().getTools().isEmpty()
        new McpServerStatusResponse().getDiscoveredTools().isEmpty()
        new SemanticSchemaResponse().getEntityTypes().isEmpty()
        new VibeIntentProposalResponse().getProposedEntities().isEmpty()
    }

    def "verify Chat domain models and lifecycle hooks"() {
        when: "creating chat entities"
        def sessionId = UUID.randomUUID()
        def mapSessionId = UUID.randomUUID()
        def layerId = UUID.randomUUID()
        def chatSession = new ChatSession(sessionId, mapSessionId, layerId, "Tactical Chat", "llama3.2")
        chatSession.onCreate()
        chatSession.onUpdate()
        chatSession.setTitle("Updated Title")
        chatSession.setActiveModel("mistral")
        chatSession.setMapSessionId(mapSessionId)
        chatSession.setTargetLayerId(layerId)
        chatSession.setId(sessionId)

        def messageId = UUID.randomUUID()
        def vibeId = UUID.randomUUID()
        def chatMessage = new ChatMessage(messageId, sessionId, com.canvais.domain.model.ChatRole.USER, "Draw area", vibeId, com.canvais.domain.model.ChatActionType.CREATE, "meta")
        chatMessage.onCreate()
        chatMessage.setContent("New Content")
        chatMessage.setRole(com.canvais.domain.model.ChatRole.ASSISTANT)
        chatMessage.setActionType(com.canvais.domain.model.ChatActionType.NONE)
        chatMessage.setVibeRecordId(vibeId)
        chatMessage.setMetadata("updated-meta")
        chatMessage.setChatSessionId(sessionId)
        chatMessage.setId(messageId)

        def entityId = UUID.randomUUID()
        def action = new com.canvais.domain.model.ChatEntityAction(UUID.randomUUID(), messageId, entityId, com.canvais.domain.model.ChatActionType.UPDATE, "{}")
        action.onCreate()
        action.setMessageId(messageId)
        action.setEntityId(entityId)
        action.setActionType(com.canvais.domain.model.ChatActionType.CREATE)
        action.setPreviousStateJson("{\"prev\": true}")

        then: "fields are verified"
        chatSession.getId() == sessionId
        chatSession.getTitle() == "Updated Title"
        chatSession.getActiveModel() == "mistral"
        chatSession.getMapSessionId() == mapSessionId
        chatSession.getTargetLayerId() == layerId
        chatSession.getCreatedAt() != null
        chatSession.getUpdatedAt() != null

        chatMessage.getId() == messageId
        chatMessage.getContent() == "New Content"
        chatMessage.getRole() == com.canvais.domain.model.ChatRole.ASSISTANT
        chatMessage.getActionType() == com.canvais.domain.model.ChatActionType.NONE
        chatMessage.getVibeRecordId() == vibeId
        chatMessage.getMetadata() == "updated-meta"
        chatMessage.getTimestamp() != null

        action.getMessageId() == messageId
        action.getEntityId() == entityId
        action.getActionType() == com.canvais.domain.model.ChatActionType.CREATE
        action.getPreviousStateJson().contains("prev")
        action.getExecutedAt() != null

        com.canvais.domain.model.ChatRole.values().length == 3
        com.canvais.domain.model.ChatActionType.values().length == 5
        com.canvais.domain.model.AiServiceStatus.values().length == 4
        com.canvais.domain.model.LayerType.OVERLAY != null
    }

    def "verify Chat DTOs and results"() {
        when: "populating chat DTOs"
        def req1 = new com.canvais.api.dto.request.CreateChatSessionRequest(UUID.randomUUID(), "Title")
        req1.setMapSessionId(req1.getMapSessionId())
        req1.setTitle("New Title")

        def req2 = new com.canvais.api.dto.request.SendChatMessageRequest("Hello", UUID.randomUUID(), null, null)
        req2.setMessage("Updated Msg")
        req2.setVibeRecordId(null)
        req2.setViewportBounds(null)
        req2.setCenterCoordinates(null)

        def req3 = new com.canvais.api.dto.request.IngestVibeToChatRequest(UUID.randomUUID(), "transcript", [], null)
        req3.setVibeRecordId(null)
        req3.setVibeTranscript("updated transcript")
        req3.setGesturePoints([])
        req3.setViewportBounds(null)

        def health = new com.canvais.api.dto.response.AiHealthResponse(com.canvais.domain.model.AiServiceStatus.AVAILABLE, "llama3.2", "http://localhost", 20L, "OK")
        health.setStatus(com.canvais.domain.model.AiServiceStatus.BUSY)
        health.setActiveModel("mistral")
        health.setEndpoint("http://remote")
        health.setLatencyMs(40L)
        health.setMessage("Busy")

        def msgDto = new com.canvais.api.dto.response.ChatMessageDto(UUID.randomUUID(), com.canvais.domain.model.ChatRole.USER, "Text", null, com.canvais.domain.model.ChatActionType.NONE, Instant.now())
        msgDto.setId(msgDto.getId())
        msgDto.setRole(com.canvais.domain.model.ChatRole.SYSTEM)
        msgDto.setContent("System Msg")
        msgDto.setVibeRecordId(null)
        msgDto.setActionType(com.canvais.domain.model.ChatActionType.QUERY)
        msgDto.setTimestamp(Instant.now())

        def sessionResp = new com.canvais.api.dto.response.ChatSessionResponse(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Title", "llama3.2", Instant.now())
        sessionResp.setId(sessionResp.getId())
        sessionResp.setMapSessionId(sessionResp.getMapSessionId())
        sessionResp.setTargetLayerId(sessionResp.getTargetLayerId())
        sessionResp.setTitle("Resp Title")
        sessionResp.setActiveModel("llama3.2")
        sessionResp.setCreatedAt(Instant.now())

        def detailResp = new com.canvais.api.dto.response.ChatSessionDetailResponse(sessionResp, [msgDto])
        detailResp.setSession(sessionResp)
        detailResp.setMessages([msgDto])

        def turnResp = new com.canvais.api.dto.response.ChatMessageTurnResponse(msgDto, msgDto, com.canvais.domain.model.ChatActionType.CREATE, UUID.randomUUID(), [])
        turnResp.setUserMessage(msgDto)
        turnResp.setAssistantMessage(msgDto)
        turnResp.setActionType(com.canvais.domain.model.ChatActionType.UPDATE)
        turnResp.setTargetLayerId(sessionResp.getTargetLayerId())
        turnResp.setAffectedEntities([])

        def clearResp = new com.canvais.api.dto.response.ClearLayerResponse(UUID.randomUUID(), 5)
        clearResp.setLayerId(clearResp.getLayerId())
        clearResp.setClearedEntitiesCount(10)

        def spatialResult = new com.canvais.infrastructure.ai.AiSpatialResult("Done", com.canvais.domain.model.ChatActionType.CREATE, null, [])
        spatialResult.setAssistantMessage("Updated Done")
        spatialResult.setActionType(com.canvais.domain.model.ChatActionType.QUERY)
        spatialResult.setTargetEntityId(UUID.randomUUID())
        spatialResult.setEntities([])

        then: "all properties match"
        req1.getTitle() == "New Title"
        req2.getMessage() == "Updated Msg"
        req3.getVibeTranscript() == "updated transcript"
        health.getStatus() == com.canvais.domain.model.AiServiceStatus.BUSY
        msgDto.getContent() == "System Msg"
        sessionResp.getTitle() == "Resp Title"
        detailResp.getMessages().size() == 1
        turnResp.getActionType() == com.canvais.domain.model.ChatActionType.UPDATE
        clearResp.getClearedEntitiesCount() == 10
        spatialResult.getAssistantMessage() == "Updated Done"
    }
}
