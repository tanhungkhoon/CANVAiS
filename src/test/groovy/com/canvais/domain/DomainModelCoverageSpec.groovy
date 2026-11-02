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
}
