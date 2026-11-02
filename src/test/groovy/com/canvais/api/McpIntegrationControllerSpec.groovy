package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.controller.McpIntegrationController
import com.canvais.api.dto.request.RegisterMcpServerRequest
import com.canvais.api.dto.response.McpDiscoveryResponse
import com.canvais.api.dto.response.McpServerStatusResponse
import com.canvais.domain.model.ConnectionStatus
import com.canvais.domain.model.TransportType
import com.canvais.service.McpIntegrationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * WebMvc unit test specification verifying McpIntegrationController endpoints.
 */
class McpIntegrationControllerSpec extends SpecificationBase {

    MockMvc mockMvc
    McpIntegrationService integrationService = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        def controller = new McpIntegrationController(integrationService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "POST /api/v1/integrations/mcp should register server and return 201"() {
        given: "a registration request"
        def request = new RegisterMcpServerRequest(
                name: "GIS MCP",
                transportType: TransportType.SSE,
                endpointUri: "http://localhost:8081/mcp/sse"
        )
        def response = new McpServerStatusResponse(
                id: "gis-mcp",
                name: "GIS MCP",
                transportType: TransportType.SSE,
                status: ConnectionStatus.CONNECTED,
                discoveredTools: ["buffer_zone"]
        )

        integrationService.registerServer(_ as RegisterMcpServerRequest) >> response

        when: "POST request is executed"
        def result = mockMvc.perform(post("/api/v1/integrations/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "status is 201 Created and JSON matches"
        result.andExpect(status().isCreated())
                .andExpect(jsonPath('$.id').value("gis-mcp"))
                .andExpect(jsonPath('$.status').value("CONNECTED"))
    }

    def "GET /api/v1/integrations/mcp should list servers"() {
        given: "list of servers in service"
        def s1 = new McpServerStatusResponse(id: "gis-mcp", name: "GIS MCP")
        integrationService.listServers() >> [s1]

        when: "GET request is executed"
        def result = mockMvc.perform(get("/api/v1/integrations/mcp"))

        then: "status is 200 OK with server"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.length()').value(1))
    }

    def "POST /api/v1/integrations/mcp/{serverId}/discover should return discovered tools"() {
        given: "discovery response"
        def discovery = new McpDiscoveryResponse(serverId: "gis-mcp", tools: ["contour_lines"])
        integrationService.discoverCapabilities("gis-mcp") >> discovery

        when: "POST discover is called"
        def result = mockMvc.perform(post("/api/v1/integrations/mcp/gis-mcp/discover"))

        then: "status is 200 OK and tools returned"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.serverId').value("gis-mcp"))
                .andExpect(jsonPath('$.tools[0]').value("contour_lines"))
    }
}
