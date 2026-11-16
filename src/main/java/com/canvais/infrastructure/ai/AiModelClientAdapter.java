package com.canvais.infrastructure.ai;

import com.canvais.api.dto.request.BoundingBoxDto;
import com.canvais.api.dto.request.GesturePointDto;
import com.canvais.api.dto.response.AiHealthResponse;
import com.canvais.domain.model.ChatMessage;
import com.canvais.domain.model.GraphicalEntity;

import java.util.List;

/**
 * Adapter interface abstracting local LLM interactions for spatial reasoning and conversational sketching.
 */
public interface AiModelClientAdapter {

    /**
     * Interprets a user prompt in the context of recent messages, active layer entities, and geographic bounds.
     *
     * @param userPrompt user natural language instruction
     * @param conversationHistory prior messages in this session
     * @param existingLayerEntities current entities on the overlay layer
     * @param bounds visible map bounds
     * @param centerCoordinates center coordinate anchor
     * @return structured spatial result
     */
    AiSpatialResult generateSpatialIntent(
            String userPrompt,
            List<ChatMessage> conversationHistory,
            List<GraphicalEntity> existingLayerEntities,
            BoundingBoxDto bounds,
            GesturePointDto centerCoordinates
    );

    /**
     * Checks availability and connectivity of the local LLM runtime.
     *
     * @return AI provider health report
     */
    AiHealthResponse checkHealth();
}
