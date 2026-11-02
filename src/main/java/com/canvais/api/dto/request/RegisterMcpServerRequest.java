package com.canvais.api.dto.request;

import com.canvais.domain.model.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for registering an external MCP server connection.
 */
public class RegisterMcpServerRequest {

    private String id;

    @NotBlank(message = "Server name is required")
    private String name;

    @NotNull(message = "Transport type is required (SSE or STDIO)")
    private TransportType transportType;

    @NotBlank(message = "Endpoint URI or command path is required")
    private String endpointUri;

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
}
