package com.canvais.infrastructure.mcp

import com.canvais.SpecificationBase
import com.canvais.domain.model.McpServerConfig
import com.canvais.domain.model.TransportType

/**
 * Spock behavioral specification for McpClientAdapter.
 */
class McpClientAdapterSpec extends SpecificationBase {

    McpClientAdapter adapter = new McpClientAdapter()

    def "should test connection and list tools from MCP server config"() {
        given: "an MCP config"
        def config = new McpServerConfig("test-mcp", "Disaster MCP", TransportType.SSE, "http://localhost:8081/mcp/sse")

        when: "testing connection"
        boolean connected = adapter.testConnection(config)

        then: "connected is true"
        connected

        when: "listing tools"
        def tools = adapter.listTools(config)

        then: "tools list is non-empty"
        tools.size() >= 1
        tools.contains("plot_defense_perimeter")
    }

    def "should execute tool call and return success status"() {
        given: "tool call parameters"
        def config = new McpServerConfig("test-mcp", "Disaster MCP", TransportType.SSE, "http://localhost:8081/mcp/sse")

        when: "calling tool"
        def result = adapter.callTool(config, "plot_defense_perimeter", [radius: 500])

        then: "status is SUCCESS"
        result.status == "SUCCESS"
        result.tool == "plot_defense_perimeter"
    }
}
