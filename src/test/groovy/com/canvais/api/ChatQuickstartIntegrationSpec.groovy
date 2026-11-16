package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.BoundingBoxDto
import com.canvais.api.dto.request.CreateChatSessionRequest
import com.canvais.api.dto.request.CreateSessionRequest
import com.canvais.api.dto.request.GesturePointDto
import com.canvais.api.dto.request.IngestVibeToChatRequest
import com.canvais.api.dto.request.SendChatMessageRequest
import com.canvais.api.dto.response.ProposedEntityDto
import com.canvais.domain.model.AiServiceStatus
import com.canvais.domain.model.ChatActionType
import com.canvais.domain.model.ChatRole
import com.canvais.domain.model.EntityStyle
import com.canvais.domain.model.LayerType
import com.canvais.infrastructure.ai.AiModelClientAdapter
import com.canvais.infrastructure.ai.AiSpatialResult
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * End-to-end integration specification validating Scenarios 1 through 7 from quickstart.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ChatQuickstartIntegrationSpec extends SpecificationBase {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    static class TestAiModelClientAdapter implements AiModelClientAdapter {
        @Override
        com.canvais.api.dto.response.AiHealthResponse checkHealth() {
            return new com.canvais.api.dto.response.AiHealthResponse(
                    AiServiceStatus.AVAILABLE,
                    "llama3.2",
                    "http://localhost:11434",
                    42L,
                    "Local Ollama mock connected"
            )
        }

        @Override
        AiSpatialResult generateSpatialIntent(
                String prompt,
                List<com.canvais.domain.model.ChatMessage> conversationHistory,
                List<com.canvais.domain.model.GraphicalEntity> existingLayerEntities,
                BoundingBoxDto bounds,
                GesturePointDto centerCoordinates) {

            if (prompt != null && (prompt.contains("High Security Zone") || prompt.contains("red"))) {
                def targetId = existingLayerEntities && !existingLayerEntities.isEmpty() ? existingLayerEntities[0].getId() : null
                def updatedStyle = new EntityStyle("#FF0000", "#FF000033", 3.0f, null, null)
                def updated = new ProposedEntityDto(
                        name: "High Security Zone",
                        entityType: "PerimeterZone",
                        style: updatedStyle
                )
                return new AiSpatialResult("Modified to red perimeter", ChatActionType.UPDATE, targetId, [updated])
            }

            def proposed = new ProposedEntityDto(
                    name: "Central Observation Perimeter",
                    entityType: "PerimeterZone",
                    geometry: [
                            type: "Polygon",
                            coordinates: [[[8.18, 48.17], [8.22, 48.17], [8.22, 48.19], [8.18, 48.19], [8.18, 48.17]]]
                    ],
                    style: new EntityStyle("#E76F51", "#E76F5133", 2.5f, null, null),
                    confidence: 0.95
            )
            return new AiSpatialResult("Synthesized observation perimeter", ChatActionType.CREATE, null, [proposed])
        }
    }

    @TestConfiguration
    static class MockAiConfig {
        @Bean
        @Primary
        AiModelClientAdapter testAiModelClientAdapter() {
            return new TestAiModelClientAdapter()
        }
    }

    def "execute end-to-end quickstart scenarios 1 through 7"() {
        when: "Scenario 1: AI Service Health Check"
        def healthResult = mockMvc.perform(get("/api/v1/chat/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.status').value("AVAILABLE"))
                .andExpect(jsonPath('$.activeModel').value("llama3.2"))
                .andReturn()

        then: "health check passes"
        healthResult.response.status == 200

        when: "Scenario 2: Create Map Session & Initialize Chat Conversation"
        def sessionReq = new CreateSessionRequest(
                title: "Tactical Grid Alpha",
                description: "Base cartography",
                centerLat: 48.1800d,
                centerLng: 8.2000d,
                zoomLevel: 12
        )
        def sessionJson = mockMvc.perform(post("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessionReq)))
                .andExpect(status().isCreated())
                .andReturn().response.contentAsString
        def mapSessionId = UUID.fromString(objectMapper.readTree(sessionJson).get("id").asText())

        def chatSessionReq = new CreateChatSessionRequest(mapSessionId, "Tactical Chat Session")
        def chatJson = mockMvc.perform(post("/api/v1/chat/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatSessionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.mapSessionId').value(mapSessionId.toString()))
                .andReturn().response.contentAsString
        def chatSessionId = UUID.fromString(objectMapper.readTree(chatJson).get("id").asText())
        def targetLayerId = UUID.fromString(objectMapper.readTree(chatJson).get("targetLayerId").asText())

        then: "session created with dedicated overlay layer"
        chatSessionId != null
        targetLayerId != null

        when: "Scenario 3: Sketch an Observation Perimeter via Conversational Prompt"
        def promptReq = new SendChatMessageRequest(
                "Draw a secure perimeter around the central communications hub",
                null,
                new BoundingBoxDto(48.20, 48.16, 8.23, 8.17),
                new GesturePointDto(48.18, 8.20)
        )
        def turnJson = mockMvc.perform(post("/api/v1/chat/sessions/${chatSessionId}/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.actionType').value("CREATE"))
                .andExpect(jsonPath('$.affectedEntities[0].name').value("Central Observation Perimeter"))
                .andReturn().response.contentAsString

        then: "perimeter object created on overlay layer"
        turnJson.contains("Central Observation Perimeter")

        when: "Scenario 4: Ingest Recorded Vibe Input into Chat"
        def vibeReq = new IngestVibeToChatRequest(
                UUID.randomUUID(),
                "Plot a transit corridor following the main ridge",
                [new GesturePointDto(48.18, 8.20)],
                new BoundingBoxDto(48.22, 48.16, 8.25, 8.18)
        )
        def vibeTurnJson = mockMvc.perform(post("/api/v1/chat/sessions/${chatSessionId}/vibe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vibeReq)))
                .andExpect(status().isOk())
                .andReturn().response.contentAsString

        then: "vibe record was processed into chat stream"
        vibeTurnJson.contains("Synthesized observation perimeter")

        when: "Scenario 5: Multi-Turn Iterative Refinement"
        def refineReq = new SendChatMessageRequest(
                "Change line color to red and rename to High Security Zone",
                null,
                new BoundingBoxDto(48.20, 48.16, 8.23, 8.17),
                new GesturePointDto(48.18, 8.20)
        )
        def refineJson = mockMvc.perform(post("/api/v1/chat/sessions/${chatSessionId}/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refineReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.actionType').value("UPDATE"))
                .andExpect(jsonPath('$.affectedEntities[0].name').value("High Security Zone"))
                .andReturn().response.contentAsString

        then: "entity updated in-place on overlay layer"
        refineJson.contains("High Security Zone")

        when: "Scenario 6: Layer Isolation & Clear Verification"
        def clearJson = mockMvc.perform(delete("/api/v1/chat/sessions/${chatSessionId}/layer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.layerId').value(targetLayerId.toString()))
                .andReturn().response.contentAsString

        then: "overlay layer cleared cleanly"
        clearJson.contains("clearedEntitiesCount")

        when: "Scenario 7: Chat History Retrieval"
        def historyJson = mockMvc.perform(get("/api/v1/chat/sessions/${chatSessionId}"))
                .andExpect(status().isOk())
                .andReturn().response.contentAsString

        then: "full multi-turn history retrieved"
        historyJson.contains("High Security Zone")
    }
}
