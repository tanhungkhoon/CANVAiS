package com.canvais.infrastructure.ai;

import com.canvais.api.dto.request.BoundingBoxDto;
import com.canvais.api.dto.request.GesturePointDto;
import com.canvais.api.dto.response.AiHealthResponse;
import com.canvais.api.dto.response.ProposedEntityDto;
import com.canvais.domain.model.AiServiceStatus;
import com.canvais.domain.model.ChatActionType;
import com.canvais.domain.model.ChatMessage;
import com.canvais.domain.model.ChatRole;
import com.canvais.domain.model.EntityStyle;
import com.canvais.domain.model.GraphicalEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of AiModelClientAdapter leveraging Spring AI Ollama ChatModel
 * with structured GeoJSON prompt generation and graceful offline fallback.
 */
@Component
public class OllamaChatModelAdapter implements AiModelClientAdapter {

    private static final Logger log = LoggerFactory.getLogger(OllamaChatModelAdapter.class);
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

    private final ChatModel chatModel;
    private final SpatialReasoningAdapter fallbackAdapter;
    private final String modelName;
    private final String baseUrl;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;

    @Autowired
    public OllamaChatModelAdapter(
            @Autowired(required = false) ChatModel chatModel,
            SpatialReasoningAdapter fallbackAdapter,
            @Value("${spring.ai.ollama.chat.options.model:llama3.2}") String modelName,
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${canvais.chat.request-timeout-seconds:30}") int timeoutSeconds) {
        this.chatModel = chatModel;
        this.fallbackAdapter = fallbackAdapter;
        this.modelName = modelName;
        this.baseUrl = baseUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AiSpatialResult generateSpatialIntent(
            String userPrompt,
            List<ChatMessage> conversationHistory,
            List<GraphicalEntity> existingLayerEntities,
            BoundingBoxDto bounds,
            GesturePointDto centerCoordinates) {

        if (chatModel == null) {
            log.warn("Spring AI ChatModel bean is not available; invoking heuristic fallback.");
            return executeFallback(userPrompt, bounds, centerCoordinates, "Local Ollama service bean is not configured.");
        }

        try {
            Prompt prompt = buildStructuredPrompt(userPrompt, conversationHistory, existingLayerEntities, bounds, centerCoordinates);
            ChatResponse response = chatModel.call(prompt);

            if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
                return executeFallback(userPrompt, bounds, centerCoordinates, "Received empty response from local model.");
            }

            String content = response.getResult().getOutput().getContent();
            return parseModelOutput(content, bounds, centerCoordinates);

        } catch (Exception ex) {
            log.warn("Error invoking local Ollama model ({}): {}", baseUrl, ex.getMessage());
            return executeFallback(userPrompt, bounds, centerCoordinates,
                    "Local AI model is unavailable or timed out. Heuristic fallback applied.");
        }
    }

    @Override
    public AiHealthResponse checkHealth() {
        if (chatModel == null) {
            return new AiHealthResponse(AiServiceStatus.UNAVAILABLE, modelName, baseUrl, 0L,
                    "Spring AI ChatModel is not initialized.");
        }
        Instant start = Instant.now();
        try {
            Prompt pingPrompt = new Prompt(new UserMessage("Respond with 'OK' only."));
            ChatResponse response = chatModel.call(pingPrompt);
            long latency = Duration.between(start, Instant.now()).toMillis();
            if (response != null && response.getResult() != null) {
                return new AiHealthResponse(AiServiceStatus.AVAILABLE, modelName, baseUrl, latency,
                        "Local Ollama service connected successfully.");
            }
            return new AiHealthResponse(AiServiceStatus.DEGRADED, modelName, baseUrl, latency,
                    "Ollama connected but returned empty output.");
        } catch (Exception ex) {
            long latency = Duration.between(start, Instant.now()).toMillis();
            return new AiHealthResponse(AiServiceStatus.UNAVAILABLE, modelName, baseUrl, latency,
                    "Cannot reach Ollama at " + baseUrl + ": " + ex.getMessage());
        }
    }

    private Prompt buildStructuredPrompt(
            String userPrompt,
            List<ChatMessage> conversationHistory,
            List<GraphicalEntity> existingLayerEntities,
            BoundingBoxDto bounds,
            GesturePointDto centerCoordinates) {

        StringBuilder systemInstruction = new StringBuilder();
        systemInstruction.append("You are the CANVAiS Spatial AI Sketching Assistant. Your job is to translate user conversational instructions ");
        systemInstruction.append("into clean, valid GeoJSON geographical objects plotted onto an overlay layer above a base map.\n");
        systemInstruction.append("You MUST return ONLY valid JSON matching this exact schema with no extra surrounding text or markdown ticks:\n");
        systemInstruction.append("{\n");
        systemInstruction.append("  \"assistantMessage\": \"Description of what was drawn or modified for the user\",\n");
        systemInstruction.append("  \"action\": \"CREATE | UPDATE | DELETE | QUERY | NONE\",\n");
        systemInstruction.append("  \"targetEntityId\": \"optional-uuid-string-if-updating-or-deleting\",\n");
        systemInstruction.append("  \"objects\": [\n");
        systemInstruction.append("    {\n");
        systemInstruction.append("      \"name\": \"Feature Name\",\n");
        systemInstruction.append("      \"entityType\": \"PerimeterZone | TransitCorridor | ObservationPoint | Custom\",\n");
        systemInstruction.append("      \"geometryType\": \"Point | LineString | Polygon\",\n");
        systemInstruction.append("      \"coordinates\": [GeoJSON coordinates with lon first, lat second: [lng, lat]],\n");
        systemInstruction.append("      \"style\": { \"strokeColor\": \"#E76F51\", \"fillColor\": \"#E76F5133\", \"strokeWidth\": 2.5 },\n");
        systemInstruction.append("      \"confidence\": 0.95\n");
        systemInstruction.append("    }\n");
        systemInstruction.append("  ]\n");
        systemInstruction.append("}\n\n");

        if (bounds != null) {
            systemInstruction.append(String.format("Current Map Viewport Bounds: North=%.4f, South=%.4f, East=%.4f, West=%.4f\n",
                    bounds.getNorth(), bounds.getSouth(), bounds.getEast(), bounds.getWest()));
        }
        if (centerCoordinates != null) {
            systemInstruction.append(String.format("Current Map Center: Lat=%.4f, Lng=%.4f\n",
                    centerCoordinates.getLat(), centerCoordinates.getLng()));
        }

        if (existingLayerEntities != null && !existingLayerEntities.isEmpty()) {
            systemInstruction.append("\nCurrently Existing Sketched Entities on the Overlay Layer:\n");
            for (GraphicalEntity ent : existingLayerEntities) {
                systemInstruction.append(String.format("- ID: %s | Name: %s | Type: %s | Geometry: %s\n",
                        ent.getId(), ent.getName(), ent.getEntityType(), ent.getGeometryType()));
            }
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemInstruction.toString()));

        if (conversationHistory != null) {
            int startIdx = Math.max(0, conversationHistory.size() - 6);
            for (int i = startIdx; i < conversationHistory.size(); i++) {
                ChatMessage hist = conversationHistory.get(i);
                if (hist.getRole() == ChatRole.USER) {
                    messages.add(new UserMessage(hist.getContent()));
                } else if (hist.getRole() == ChatRole.ASSISTANT) {
                    messages.add(new AssistantMessage(hist.getContent()));
                }
            }
        }

        messages.add(new UserMessage(userPrompt));
        return new Prompt(messages);
    }

    private AiSpatialResult parseModelOutput(String content, BoundingBoxDto bounds, GesturePointDto centerCoordinates) {
        try {
            String jsonContent = extractJson(content);
            JsonNode root = objectMapper.readTree(jsonContent);

            String assistantMessage = root.path("assistantMessage").asText("Spatial object synthesized.");
            String actionStr = root.path("action").asText("CREATE").toUpperCase();
            ChatActionType actionType;
            try {
                actionType = ChatActionType.valueOf(actionStr);
            } catch (Exception ex) {
                actionType = ChatActionType.CREATE;
            }

            UUID targetEntityId = null;
            if (root.hasNonNull("targetEntityId")) {
                try {
                    targetEntityId = UUID.fromString(root.get("targetEntityId").asText());
                } catch (Exception ignored) {
                }
            }

            List<ProposedEntityDto> entities = new ArrayList<>();
            JsonNode objectsNode = root.path("objects");
            if (objectsNode.isArray()) {
                for (JsonNode objNode : objectsNode) {
                    ProposedEntityDto entity = new ProposedEntityDto();
                    entity.setName(objNode.path("name").asText("Sketched Entity"));
                    entity.setEntityType(objNode.path("entityType").asText("Custom"));

                    String geomType = objNode.path("geometryType").asText("Point");
                    JsonNode coordsNode = objNode.path("coordinates");
                    Map<String, Object> geomMap = Map.of(
                            "type", geomType,
                            "coordinates", objectMapper.convertValue(coordsNode, Object.class)
                    );
                    entity.setGeometry(geomMap);

                    if (objNode.has("style")) {
                        EntityStyle style = objectMapper.convertValue(objNode.get("style"), EntityStyle.class);
                        entity.setStyle(style);
                    }
                    entity.setConfidence(objNode.path("confidence").asDouble(0.92));
                    entity.getSemanticAttributes().put("synthesizedBy", "Ollama-Chat");
                    entities.add(entity);
                }
            }

            if (entities.isEmpty() && actionType == ChatActionType.CREATE) {
                return executeFallback(assistantMessage, bounds, centerCoordinates, assistantMessage);
            }

            return new AiSpatialResult(assistantMessage, actionType, targetEntityId, entities);

        } catch (Exception ex) {
            log.warn("Failed to parse LLM JSON output: {}. Falling back.", ex.getMessage());
            return executeFallback(content, bounds, centerCoordinates, "Heuristic fallback generated.");
        }
    }

    private String extractJson(String text) {
        if (text == null) {
            return "{}";
        }
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return text.trim();
    }

    private AiSpatialResult executeFallback(String prompt, BoundingBoxDto bounds, GesturePointDto center, String narrative) {
        List<GesturePointDto> strokePoints = center != null ? List.of(center) : Collections.emptyList();
        List<ProposedEntityDto> fallbackEntities = fallbackAdapter.synthesizeEntities(prompt, strokePoints, bounds);
        return new AiSpatialResult(
                narrative != null ? narrative : "Heuristic fallback spatial entity generated.",
                ChatActionType.CREATE,
                null,
                fallbackEntities
        );
    }
}
