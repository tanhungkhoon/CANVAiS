package com.canvais.api.dto.response;

import com.canvais.domain.model.ConnectionStatus;
import com.canvais.domain.model.TransportType;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO describing the connectivity status of an MCP server.
 */
public class McpServerStatusResponse {

    private String id;
    private String name;
    private TransportType transportType;
    private String endpointUri;
    private ConnectionStatus status;
    private List<String> discoveredTools = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public String getEndpointUri() {
        return endpointUri;
    }

    public void setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public List<String> getDiscoveredTools() {
        return discoveredTools;
    }

    public void setDiscoveredTools(List<String> discoveredTools) {
        this.discoveredTools = discoveredTools;
    }
}
