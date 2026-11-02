package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.AcceptProposalRequest
import com.canvais.api.dto.request.CreateEntityRequest
import com.canvais.api.dto.request.VibeIntentProposeRequest
import com.canvais.domain.model.EntityStyle
import com.canvais.domain.model.GeometryType
import com.canvais.domain.model.GraphicalEntity
import com.canvais.domain.model.LayerType
import com.canvais.domain.model.MapLayer
import com.canvais.domain.model.MapSession
import com.canvais.domain.model.McpServerConfig
import com.canvais.domain.model.SemanticSchema
import com.canvais.domain.model.TransportType
import com.canvais.domain.model.VibeIntentProposal
import com.canvais.domain.repository.GraphicalEntityRepository
import com.canvais.domain.repository.MapLayerRepository
import com.canvais.domain.repository.MapSessionRepository
import com.canvais.domain.repository.McpServerConfigRepository
import com.canvais.domain.repository.SemanticSchemaRepository
import com.canvais.domain.repository.VibeIntentProposalRepository
import com.canvais.infrastructure.ai.SpatialReasoningAdapter
import com.canvais.infrastructure.geospatial.GeoJsonConverter
import com.canvais.infrastructure.geospatial.JtsGeometryAdapter
import com.canvais.infrastructure.mcp.McpClientAdapter

/**
 * Specification verifying comprehensive service queries and mapping paths.
 */
class ServiceIntegrationCoverageSpec extends SpecificationBase {

    MapSessionRepository sessionRepository = Mock()
    MapLayerRepository layerRepository = Mock()
    GraphicalEntityRepository entityRepository = Mock()
    McpServerConfigRepository mcpRepository = Mock()
    SemanticSchemaRepository schemaRepository = Mock()
    VibeIntentProposalRepository proposalRepository = Mock()

    JtsGeometryAdapter geometryAdapter = new JtsGeometryAdapter()
    GeoJsonConverter geoJsonConverter = new GeoJsonConverter()
    McpClientAdapter mcpClientAdapter = new McpClientAdapter()
    SpatialReasoningAdapter spatialReasoningAdapter = new SpatialReasoningAdapter()

    LayerService layerService
    MapSessionService sessionService
    GraphicalEntityService entityService
    SemanticSchemaService schemaService
    McpIntegrationService mcpService
    VibeIntentFusionService fusionService

    def setup() {
        layerService = new LayerService(layerRepository, sessionRepository)
        sessionService = new MapSessionService(sessionRepository, layerService)
        entityService = new GraphicalEntityService(entityRepository, layerRepository, geometryAdapter, geoJsonConverter)
        schemaService = new SemanticSchemaService(schemaRepository)
        mcpService = new McpIntegrationService(mcpRepository, mcpClientAdapter, schemaService)
        fusionService = new VibeIntentFusionService(proposalRepository, spatialReasoningAdapter, entityService)
    }

    def "should list sessions and retrieve session detail"() {
        given: "sessions and layers in mocks"
        def sessionId = UUID.randomUUID()
        def session = new MapSession("Test Session", "Desc", 48.0, 8.0, 10)
        session.id = sessionId
        session.onCreate()

        def layer = new MapLayer(sessionId, "Base Layer", 0, LayerType.STANDARD)
        layer.id = UUID.randomUUID()

        sessionRepository.findAll() >> [session]
        sessionRepository.findById(sessionId) >> Optional.of(session)
        layerRepository.findBySessionIdOrderByOrderIndexAsc(sessionId) >> [layer]

        when: "listSessions is called"
        def list = sessionService.listSessions()

        then: "list has 1 element"
        list.size() == 1
        list[0].id == sessionId

        when: "getSessionDetail is called"
        def detail = sessionService.getSessionDetail(sessionId)

        then: "detail contains session and layers"
        detail.id == sessionId
        detail.layers.size() == 1
        detail.layers[0].name == "Base Layer"
    }

    def "should retrieve layer by id and list entities"() {
        given: "a layer with entities"
        def layerId = UUID.randomUUID()
        def layer = new MapLayer(UUID.randomUUID(), "Layer 1", 1, LayerType.STANDARD)
        layer.id = layerId

        def entity = new GraphicalEntity(layerId, "Post", GeometryType.POINT, '{"type":"Point","coordinates":[8.2,48.1]}')
        entity.id = UUID.randomUUID()
        entity.name = "Entity 1"
        entity.style = new EntityStyle("#000", "#111", 2.0f, null, null)

        layerRepository.findById(layerId) >> Optional.of(layer)
        layerRepository.existsById(layerId) >> true
        entityRepository.findByLayerId(layerId) >> [entity]

        when: "layer is retrieved"
        def resLayer = layerService.getLayer(layerId)

        then:
        resLayer.id == layerId

        when: "entities listed by layer"
        def entities = entityService.listEntitiesByLayer(layerId)

        then:
        entities.size() == 1
        entities[0].name == "Entity 1"

        when: "feature collection retrieved"
        def featureCollection = entityService.getLayerFeatureCollection(layerId)

        then:
        featureCollection.type == "FeatureCollection"
        ((List) featureCollection.features).size() == 1
    }

    def "should create entity on layer and validate geometry"() {
        given: "a layer and entity create request"
        def layerId = UUID.randomUUID()
        layerRepository.existsById(layerId) >> true

        def request = new CreateEntityRequest(
                name: "Observation Point",
                entityType: "PointOfInterest",
                geometry: [type: "Point", coordinates: [8.20, 48.18]],
                style: new EntityStyle("#FFF", "#000", 2.0f, null, null),
                semanticAttributes: [status: "ACTIVE"]
        )

        entityRepository.save(_ as GraphicalEntity) >> { GraphicalEntity e ->
            e.id = UUID.randomUUID()
            return e
        }

        when: "creating entity"
        def response = entityService.createEntity(layerId, request)

        then: "entity created"
        response.id != null
        response.entityType == "PointOfInterest"
    }

    def "should handle missing layer when creating entity"() {
        given: "a non-existent layer"
        def layerId = UUID.randomUUID()
        layerRepository.existsById(layerId) >> false

        when:
        entityService.createEntity(layerId, new CreateEntityRequest())

        then:
        thrown(NoSuchElementException)
    }

    def "should handle missing layer when getting feature collection"() {
        given: "a non-existent layer"
        def layerId = UUID.randomUUID()
        layerRepository.existsById(layerId) >> false

        when:
        entityService.getLayerFeatureCollection(layerId)

        then:
        thrown(NoSuchElementException)
    }

    def "should list MCP servers"() {
        given: "configured servers"
        def server = new McpServerConfig("mcp-1", "Server 1", TransportType.SSE, "http://localhost:8081")
        mcpRepository.findAll() >> [server]

        when:
        def servers = mcpService.listServers()

        then:
        servers.size() == 1
        servers[0].id == "mcp-1"
    }

    def "should list schemas in SemanticSchemaService"() {
        given: "schemas in repository"
        def schema = new SemanticSchema("Schema 1", "Category", "1.0.0")
        schemaRepository.findAll() >> [schema]

        when:
        def schemas = schemaService.listSchemas()

        then:
        schemas.size() == 1
        schemas[0].name == "Schema 1"
    }

    def "should accept proposal with filtered selected tempIds"() {
        given: "a proposal with two proposed entities"
        def proposalId = UUID.randomUUID()
        def layerId = UUID.randomUUID()

        def prop = new VibeIntentProposal(layerId, "Multi entity summary")
        prop.proposalId = proposalId

        def candidate1 = new com.canvais.api.dto.response.ProposedEntityDto()
        candidate1.tempId = "temp-1"
        candidate1.entityType = "Checkpoint"
        candidate1.geometry = [type: "Point", coordinates: [8.2, 48.1]]

        def candidate2 = new com.canvais.api.dto.response.ProposedEntityDto()
        candidate2.tempId = "temp-2"
        candidate2.entityType = "Checkpoint2"
        candidate2.geometry = [type: "Point", coordinates: [8.3, 48.2]]

        prop.proposedEntities = [candidate1, candidate2]
        proposalRepository.findById(proposalId) >> Optional.of(prop)
        layerRepository.existsById(layerId) >> true
        entityRepository.save(_ as GraphicalEntity) >> { GraphicalEntity e ->
            e.id = UUID.randomUUID()
            return e
        }

        when: "accepting only temp-1"
        def acceptReq = new AcceptProposalRequest(selectedTempIds: ["temp-1"])
        def committed = fusionService.acceptProposal(proposalId, acceptReq)

        then: "only 1 entity committed"
        committed.size() == 1
    }
}
