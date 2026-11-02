package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.controller.MapSessionController
import com.canvais.api.dto.request.CreateSessionRequest
import com.canvais.api.dto.response.MapSessionResponse
import com.canvais.service.LayerService
import com.canvais.service.MapSessionService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * WebMvc unit test specification verifying MapSessionController endpoints.
 */
class MapSessionControllerSpec extends SpecificationBase {

    MockMvc mockMvc
    MapSessionService sessionService = Mock()
    LayerService layerService = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        def controller = new MapSessionController(sessionService, layerService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "POST /api/v1/sessions should create session and return 201"() {
        given: "a session creation request"
        def request = new CreateSessionRequest(
                title: "Tactical Operations Map",
                description: "Real-time command center workspace",
                centerLat: 48.1800,
                centerLng: 8.2000,
                zoomLevel: 12
        )
        def sessionId = UUID.randomUUID()
        def response = new MapSessionResponse(
                id: sessionId,
                title: "Tactical Operations Map",
                centerLat: 48.1800,
                centerLng: 8.2000,
                zoomLevel: 12
        )

        sessionService.createSession(_ as CreateSessionRequest) >> response

        when: "POST request is performed"
        def result = mockMvc.perform(post("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "response status is 201 Created and JSON fields match"
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.id').value(sessionId.toString()))
                .andExpect(jsonPath('$.title').value("Tactical Operations Map"))
                .andExpect(jsonPath('$.centerLat').value(48.18))
    }

    def "GET /api/v1/sessions should list all sessions"() {
        given: "a list of sessions in service"
        def session1 = new MapSessionResponse(id: UUID.randomUUID(), title: "Session Alpha")
        def session2 = new MapSessionResponse(id: UUID.randomUUID(), title: "Session Beta")
        sessionService.listSessions() >> [session1, session2]

        when: "GET request is performed"
        def result = mockMvc.perform(get("/api/v1/sessions"))

        then: "response is 200 OK with 2 items"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.length()').value(2))
                .andExpect(jsonPath('$[0].title').value("Session Alpha"))
    }
}
