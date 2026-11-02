package com.canvais.service;

import com.canvais.api.dto.request.RegisterMcpServerRequest;
import com.canvais.api.dto.response.McpDiscoveryResponse;
import com.canvais.api.dto.response.McpServerStatusResponse;
import com.canvais.domain.model.ConnectionStatus;
import com.canvais.domain.model.McpServerConfig;
import com.canvais.domain.repository.McpServerConfigRepository;
import com.canvais.infrastructure.mcp.McpClientAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service managing MCP server lifecycle, discovery, and tool integration.
 */
@Service
@Transactional
public class McpIntegrationService {

    private final McpServerConfigRepository repository;
    private final McpClientAdapter clientAdapter;
    private final SemanticSchemaService schemaService;

    public McpIntegrationService(McpServerConfigRepository repository,
                                 McpClientAdapter clientAdapter,
                                 SemanticSchemaService schemaService) {
        this.repository = repository;
        this.clientAdapter = clientAdapter;
        this.schemaService = schemaService;
    }

    /**
     * Registers a new MCP server.
     *
     * @param request server details
     * @return status response
     */
    public McpServerStatusResponse registerServer(RegisterMcpServerRequest request) {
        String id = request.getId();
        if (id == null || id.isBlank()) {
            id = request.getName().toLowerCase().replaceAll("\\s+", "-");
        }

        McpServerConfig config = new McpServerConfig(id, request.getName(), request.getTransportType(), request.getEndpointUri());
        boolean connected = clientAdapter.testConnection(config);
        config.setConnectionStatus(connected ? ConnectionStatus.CONNECTED : ConnectionStatus.ERROR);

        if (connected) {
            List<String> tools = clientAdapter.listTools(config);
            config.setDiscoveredTools(tools);
            config.setLastDiscoveredAt(Instant.now());
        }

        McpServerConfig saved = repository.save(config);
        return toStatusResponse(saved);
    }

    /**
     * Lists all registered MCP servers.
     *
     * @return list of status responses
     */
    @Transactional(readOnly = true)
    public List<McpServerStatusResponse> listServers() {
        return repository.findAll().stream()
                .map(this::toStatusResponse)
                .collect(Collectors.toList());
    }

    /**
     * Triggers capability discovery on an active MCP server.
     *
     * @param serverId server identifier
     * @return discovery response
     */
    public McpDiscoveryResponse discoverCapabilities(String serverId) {
        McpServerConfig config = repository.findById(serverId)
                .orElseThrow(() -> new NoSuchElementException("MCP Server not found with ID: " + serverId));

        List<String> tools = clientAdapter.listTools(config);
        config.setDiscoveredTools(tools);
        config.setLastDiscoveredAt(Instant.now());
        config.setConnectionStatus(ConnectionStatus.CONNECTED);
        repository.save(config);

        McpDiscoveryResponse response = new McpDiscoveryResponse();
        response.setServerId(serverId);
        response.setTools(tools);
        response.setSchemas(schemaService.listSchemas());
        return response;
    }

    public McpServerStatusResponse toStatusResponse(McpServerConfig config) {
        McpServerStatusResponse response = new McpServerStatusResponse();
        response.setId(config.getId());
        response.setName(config.getName());
        response.setTransportType(config.getTransportType());
        response.setEndpointUri(config.getEndpointUri());
        response.setStatus(config.getConnectionStatus());
        response.setDiscoveredTools(config.getDiscoveredTools());
        return response;
    }
}
