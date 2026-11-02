package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.CreateLayerRequest
import com.canvais.api.dto.request.CreateSessionRequest
import com.canvais.domain.model.LayerType
import com.canvais.domain.model.MapLayer
import com.canvais.domain.model.MapSession
import com.canvais.domain.repository.MapLayerRepository
import com.canvais.domain.repository.MapSessionRepository

/**
 * Spock behavioral specification for MapSessionService and LayerService.
 */
class MapSessionServiceSpec extends SpecificationBase {

    MapSessionRepository sessionRepository = Mock()
    MapLayerRepository layerRepository = Mock()
    LayerService layerService
    MapSessionService sessionService

    def setup() {
        layerService = new LayerService(layerRepository, sessionRepository)
        sessionService = new MapSessionService(sessionRepository, layerService)
    }

    def "should create session and populate default coordinate center"() {
        given: "a valid session creation request"
        def request = new CreateSessionRequest(
                title: "Alpine S&R Grid",
                description: "Search and rescue mission in high alpine terrain",
                centerLat: 46.8182,
                centerLng: 8.2275,
                zoomLevel: 13
        )

        when: "session is created"
        def response = sessionService.createSession(request)

        then: "sessionRepository persists entity"
        1 * sessionRepository.save(_ as MapSession) >> { MapSession s ->
            s.id = UUID.randomUUID()
            return s
        }

        and: "returned DTO matches input"
        response.title == "Alpine S&R Grid"
        response.centerLat == 46.8182
        response.centerLng == 8.2275
        response.zoomLevel == 13
    }

    def "should add layer to existing session"() {
        given: "an existing session ID and layer request"
        def sessionId = UUID.randomUUID()
        def layerRequest = new CreateLayerRequest(
                name: "Perimeter Checkpoints",
                orderIndex: 1,
                layerType: LayerType.STANDARD
        )

        when: "layer is created"
        def response = layerService.createLayer(sessionId, layerRequest)

        then: "session existence is checked and layer is saved"
        1 * sessionRepository.existsById(sessionId) >> true
        1 * layerRepository.save(_ as MapLayer) >> { MapLayer l ->
            l.id = UUID.randomUUID()
            return l
        }

        and: "layer attributes match"
        response.name == "Perimeter Checkpoints"
        response.orderIndex == 1
        response.layerType == LayerType.STANDARD
        response.visible == true
    }

    def "should throw NoSuchElementException when adding layer to non-existent session"() {
        given: "a non-existent session ID"
        def sessionId = UUID.randomUUID()
        def layerRequest = new CreateLayerRequest(name: "Orphan Layer")

        sessionRepository.existsById(sessionId) >> false

        when: "creating layer"
        layerService.createLayer(sessionId, layerRequest)

        then: "exception is thrown"
        thrown(NoSuchElementException)
    }
}
