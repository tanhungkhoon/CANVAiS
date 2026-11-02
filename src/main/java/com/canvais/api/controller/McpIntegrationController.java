package com.canvais.api.controller;

import com.canvais.api.dto.request.RegisterMcpServerRequest;
import com.canvais.api.dto.response.McpDiscoveryResponse;
import com.canvais.api.dto.response.McpServerStatusResponse;
import com.canvais.service.McpIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Model Context Protocol (MCP) server integration and tool discovery.
 */
@RestController
@RequestMapping("/api/v1/integrations/mcp")
@Tag(name = "MCP Integrations", description = "Endpoints for registering and querying MCP servers and AI skill plugins")
public class McpIntegrationController {

    private final McpIntegrationService integrationService;

    public McpIntegrationController(McpIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    /**
     * Lists all registered MCP servers and status.
     *
     * @return 200 OK with list of servers
     */
    @GetMapping
    @Operation(summary = "List all configured MCP servers")
    public ResponseEntity<List<McpServerStatusResponse>> listMcpServers() {
        return ResponseEntity.ok(integrationService.listServers());
    }

    /**
     * Registers a new MCP server connection.
     *
     * @param request registration parameters
     * @return 201 Created with server status
     */
    @PostMapping
    @Operation(summary = "Register a new MCP server connection")
    public ResponseEntity<McpServerStatusResponse> registerMcpServer(
            @Valid @RequestBody RegisterMcpServerRequest request) {
        McpServerStatusResponse registered = integrationService.registerServer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    /**
     * Discovers exposed tools and schemas on an MCP server.
     *
     * @param serverId server identifier
     * @return 200 OK with discovery response
     */
    @PostMapping("/{serverId}/discover")
    @Operation(summary = "Trigger tool and schema discovery on an active MCP server")
    public ResponseEntity<McpDiscoveryResponse> discoverMcpCapabilities(@PathVariable String serverId) {
        return ResponseEntity.ok(integrationService.discoverCapabilities(serverId));
    }
}
