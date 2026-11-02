package com.canvais.api.dto.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO returning tools and schemas discovered from an MCP server.
 */
public class McpDiscoveryResponse {

    private String serverId;
    private List<String> tools = new ArrayList<>();
    private List<SemanticSchemaResponse> schemas = new ArrayList<>();

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }

    public List<SemanticSchemaResponse> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<SemanticSchemaResponse> schemas) {
        this.schemas = schemas;
    }
}
