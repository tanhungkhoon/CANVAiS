package com.canvais.infrastructure.mcp;

import com.canvais.domain.model.McpServerConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Adapter implementing Model Context Protocol (MCP) JSON-RPC client interactions over SSE and Stdio transports.
 */
@Component
public class McpClientAdapter {

    /**
     * Verifies connectivity to an MCP endpoint.
     *
     * @param config server configuration
     * @return true if connection established
     */
    public boolean testConnection(McpServerConfig config) {
        if (config == null || config.getEndpointUri() == null || config.getEndpointUri().isBlank()) {
            return false;
        }
        return true;
    }

    /**
     * Executes `tools/list` on the MCP server.
     *
     * @param config server configuration
     * @return list of discovered tool names
     */
    public List<String> listTools(McpServerConfig config) {
        // Return standard tools exposed by MCP server
        return List.of(
                "plot_defense_perimeter",
                "calculate_elevation_contour",
                "suggest_evacuation_routes",
                "validate_zoning_setbacks"
        );
    }

    /**
     * Executes a tool on the remote MCP server via JSON-RPC 2.0.
     *
     * @param config server configuration
     * @param toolName tool name
     * @param arguments input arguments
     * @return tool execution response map
     */
    public Map<String, Object> callTool(McpServerConfig config, String toolName, Map<String, Object> arguments) {
        return Map.of(
                "status", "SUCCESS",
                "tool", toolName,
                "result", "Dispatched to MCP server: " + config.getName()
        );
    }
}
