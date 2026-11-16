package com.canvais.infrastructure.ai

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.BoundingBoxDto
import com.canvais.api.dto.request.GesturePointDto
import com.canvais.api.dto.response.ProposedEntityDto
import com.canvais.domain.model.ChatActionType
import com.canvais.domain.model.EntityStyle
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt

/**
 * Specification verifying fallback resilience when the local Ollama LLM is disconnected or throws timeout errors.
 */
class OllamaOfflineResilienceSpec extends SpecificationBase {

    ChatModel chatModel = Mock()
    SpatialReasoningAdapter fallbackAdapter = Mock()
    OllamaChatModelAdapter adapter

    def setup() {
        adapter = new OllamaChatModelAdapter(chatModel, fallbackAdapter, "llama3.2", "http://localhost:11434", 30)
    }

    def "generateSpatialIntent delegates to fallbackAdapter when model call throws exception"() {
        given: "a failing chatModel call"
        chatModel.call(_ as Prompt) >> { throw new RuntimeException("Read timeout after 30s") }

        def bounds = new BoundingBoxDto(48.20, 48.16, 8.23, 8.17)
        def center = new GesturePointDto(48.18, 8.20)

        def fallbackEntity = new ProposedEntityDto(
                name: "Fallback Perimeter",
                entityType: "PerimeterZone",
                style: new EntityStyle("#E76F51", null, 2.0f, null, null)
        )
        fallbackAdapter.synthesizeEntities(_ as String, _ as List, bounds) >> [fallbackEntity]

        when: "generateSpatialIntent is called"
        def result = adapter.generateSpatialIntent("Draw a perimeter", [], [], bounds, center)

        then: "result gracefully returns heuristic entities without throwing"
        result != null
        result.actionType == ChatActionType.CREATE
        result.entities.size() == 1
        result.entities[0].name == "Fallback Perimeter"
        result.assistantMessage.contains("fallback")
    }

    def "generateSpatialIntent gracefully uses fallback when chatModel is null"() {
        given: "an adapter without chatModel bean"
        def nullAdapter = new OllamaChatModelAdapter(null, fallbackAdapter, "llama3.2", "http://localhost:11434", 30)
        def bounds = new BoundingBoxDto(48.20, 48.16, 8.23, 8.17)
        def center = new GesturePointDto(48.18, 8.20)

        def fallbackEntity = new ProposedEntityDto(name: "Default Marker", entityType: "ObservationPoint")
        fallbackAdapter.synthesizeEntities(_ as String, _ as List, bounds) >> [fallbackEntity]

        when: "generateSpatialIntent is called"
        def result = nullAdapter.generateSpatialIntent("Place marker", [], [], bounds, center)

        then: "fallback entity is returned safely"
        result != null
        result.entities.size() == 1
        result.entities[0].name == "Default Marker"
    }
}
