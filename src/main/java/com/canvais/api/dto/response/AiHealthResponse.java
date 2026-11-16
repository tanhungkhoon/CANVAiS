package com.canvais.api.dto.response;

import com.canvais.domain.model.AiServiceStatus;

/**
 * Health response indicating local Ollama connectivity, model in use, and latency.
 */
public class AiHealthResponse {

    private AiServiceStatus status;
    private String activeModel;
    private String endpoint;
    private Long latencyMs;
    private String message;

    public AiHealthResponse() {
    }

    public AiHealthResponse(AiServiceStatus status, String activeModel, String endpoint, Long latencyMs, String message) {
        this.status = status;
        this.activeModel = activeModel;
        this.endpoint = endpoint;
        this.latencyMs = latencyMs;
        this.message = message;
    }

    public AiServiceStatus getStatus() {
        return status;
    }

    public void setStatus(AiServiceStatus status) {
        this.status = status;
    }

    public String getActiveModel() {
        return activeModel;
    }

    public void setActiveModel(String activeModel) {
        this.activeModel = activeModel;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
