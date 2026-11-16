package com.canvais.api.controller;

import com.canvais.api.dto.request.CreateChatSessionRequest;
import com.canvais.api.dto.request.IngestVibeToChatRequest;
import com.canvais.api.dto.request.SendChatMessageRequest;
import com.canvais.api.dto.response.AiHealthResponse;
import com.canvais.api.dto.response.ChatMessageTurnResponse;
import com.canvais.api.dto.response.ChatSessionDetailResponse;
import com.canvais.api.dto.response.ChatSessionResponse;
import com.canvais.api.dto.response.ClearLayerResponse;
import com.canvais.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for conversational AI layer sketching, Ollama health monitoring,
 * and vibe record ingestion into chat.
 */
@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat Sketching", description = "Endpoints for conversational layer sketching, Ollama LLM, and vibe ingestion")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Probes local Ollama model connectivity, status, and latency.
     *
     * @return 200 OK with AI provider health report
     */
    @GetMapping("/health")
    @Operation(summary = "Check local Ollama AI model connectivity and status")
    public ResponseEntity<AiHealthResponse> getChatAiHealth() {
        return ResponseEntity.ok(chatService.checkHealth());
    }

    /**
     * Initializes or retrieves an active chat session for a map session.
     *
     * @param request session parameters
     * @return 200 OK with initialized chat session
     */
    @PostMapping("/sessions")
    @Operation(summary = "Initialize or retrieve a chat session for a map session")
    public ResponseEntity<ChatSessionResponse> createChatSession(
            @Valid @RequestBody CreateChatSessionRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateSession(request));
    }

    /**
     * Retrieves chat session details and chronological message history.
     *
     * @param chatSessionId unique identifier of the chat session
     * @return 200 OK with session details and messages
     */
    @GetMapping("/sessions/{chatSessionId}")
    @Operation(summary = "Retrieve chat session details and message history")
    public ResponseEntity<ChatSessionDetailResponse> getChatSession(
            @PathVariable UUID chatSessionId) {
        return ResponseEntity.ok(chatService.getSessionDetail(chatSessionId));
    }

    /**
     * Sends a conversational prompt to the local AI assistant to draw or refine graphical objects.
     *
     * @param chatSessionId unique identifier of the chat session
     * @param request prompt and spatial context
     * @return 200 OK with assistant response and affected entities on the overlay layer
     */
    @PostMapping("/sessions/{chatSessionId}/messages")
    @Operation(summary = "Send a conversational prompt to the local AI to sketch or refine objects")
    public ResponseEntity<ChatMessageTurnResponse> sendChatMessage(
            @PathVariable UUID chatSessionId,
            @Valid @RequestBody SendChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(chatSessionId, request));
    }

    /**
     * Ingests a captured vibe transcript directly into the chat conversation stream.
     *
     * @param chatSessionId unique identifier of the chat session
     * @param request vibe transcript and gesture data
     * @return 200 OK with assistant response and synthesized entities
     */
    @PostMapping("/sessions/{chatSessionId}/vibe")
    @Operation(summary = "Ingest an existing vibe record into the chat stream to trigger layer sketching")
    public ResponseEntity<ChatMessageTurnResponse> ingestVibeRecordToChat(
            @PathVariable UUID chatSessionId,
            @Valid @RequestBody IngestVibeToChatRequest request) {
        return ResponseEntity.ok(chatService.ingestVibe(chatSessionId, request));
    }

    /**
     * Clears all sketched objects from the chat overlay layer.
     *
     * @param chatSessionId unique identifier of the chat session
     * @return 200 OK with count of cleared entities
     */
    @DeleteMapping("/sessions/{chatSessionId}/layer")
    @Operation(summary = "Clear all sketched objects from the chat overlay layer")
    public ResponseEntity<ClearLayerResponse> clearChatOverlayLayer(
            @PathVariable UUID chatSessionId) {
        return ResponseEntity.ok(chatService.clearOverlayLayer(chatSessionId));
    }
}
