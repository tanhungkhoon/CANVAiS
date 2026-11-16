package com.canvais.domain.model;

/**
 * Operational connectivity and readiness status of the local AI provider.
 */
public enum AiServiceStatus {
    AVAILABLE,
    BUSY,
    DEGRADED,
    UNAVAILABLE
}
