package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.RegisterMcpServerRequest
import com.canvais.domain.model.ConnectionStatus
import com.canvais.domain.model.McpServerConfig
import com.canvais.domain.model.TransportType
import com.canvais.domain.repository.McpServerConfigRepository
import com.canvais.infrastructure.mcp.McpClientAdapter

/**
 * Spock behavioral specification for McpIntegrationService.
 */
class McpIntegrationServiceSpec extends SpecificationBase {

    McpServerConfigRepository repository = Mock()
    McpClientAdapter clientAdapter = Mock()
    SemanticSchemaService schemaService = Mock()
    McpIntegrationService service

    def setup() {
        service = new McpIntegrationService(repository, clientAdapter, schemaService)
    }

    def "should register MCP server and discover tools"() {
        given: "a registration request"
        def request = new RegisterMcpServerRequest(
                name: "Tactical Defense MCP",
                transportType: TransportType.SSE,
                endpointUri: "http://localhost:8081/mcp/sse"
        )

        clientAdapter.testConnection(_ as McpServerConfig) >> true
        clientAdapter.listTools(_ as McpServerConfig) >> ["plot_corridor", "triage_zone"]

        when: "server is registered"
        def response = service.registerServer(request)

        then: "server status is CONNECTED with tools"
        1 * repository.save(_ as McpServerConfig) >> { McpServerConfig c -> return c }
        response.status == ConnectionStatus.CONNECTED
        response.discoveredTools.contains("plot_corridor")
    }

    def "should trigger discovery on existing server"() {
        given: "an existing server in repository"
        def config = new McpServerConfig("tactical-mcp", "Tactical MCP", TransportType.SSE, "http://localhost:8081/mcp/sse")
        repository.findById("tactical-mcp") >> Optional.of(config)
        clientAdapter.listTools(config) >> ["new_tool_discovered"]
        schemaService.listSchemas() >> []

        when: "discovery is triggered"
        def discovery = service.discoverCapabilities("tactical-mcp")

        then: "discovered tools match"
        discovery.serverId == "tactical-mcp"
        discovery.tools == ["new_tool_discovered"]
    }
}
