package com.canvais.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity configuring connection parameters and tracking status of an external MCP server.
 */
@Entity
@Table(name = "mcp_server_configs")
public class McpServerConfig {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @NotNull
    private String id;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportType transportType;

    @NotNull
    @Column(nullable = false, length = 500)
    private String endpointUri;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus connectionStatus;

    private Instant lastDiscoveredAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String discoveredToolsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String associatedLayerIdsJson;

    public McpServerConfig() {
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
    }

    public McpServerConfig(String id, String name, TransportType transportType, String endpointUri) {
        this();
        this.id = id;
        this.name = name;
        this.transportType = transportType;
        this.endpointUri = endpointUri;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.connectionStatus == null) {
            this.connectionStatus = ConnectionStatus.CONNECTED;
        }
    }

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

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public Instant getLastDiscoveredAt() {
        return lastDiscoveredAt;
    }

    public void setLastDiscoveredAt(Instant lastDiscoveredAt) {
        this.lastDiscoveredAt = lastDiscoveredAt;
    }

    public List<String> getDiscoveredTools() {
        if (discoveredToolsJson == null || discoveredToolsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(discoveredToolsJson, new TypeReference<List<String>>() { });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setDiscoveredTools(List<String> tools) {
        if (tools == null || tools.isEmpty()) {
            this.discoveredToolsJson = null;
        } else {
            try {
                this.discoveredToolsJson = OBJECT_MAPPER.writeValueAsString(tools);
            } catch (JsonProcessingException e) {
                this.discoveredToolsJson = null;
            }
        }
    }

    public List<UUID> getAssociatedLayerIds() {
        if (associatedLayerIdsJson == null || associatedLayerIdsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(associatedLayerIdsJson, new TypeReference<List<UUID>>() { });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setAssociatedLayerIds(List<UUID> layerIds) {
        if (layerIds == null || layerIds.isEmpty()) {
            this.associatedLayerIdsJson = null;
        } else {
            try {
                this.associatedLayerIdsJson = OBJECT_MAPPER.writeValueAsString(layerIds);
            } catch (JsonProcessingException e) {
                this.associatedLayerIdsJson = null;
            }
        }
    }
}
