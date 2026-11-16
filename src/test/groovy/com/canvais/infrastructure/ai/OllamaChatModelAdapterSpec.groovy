package com.canvais.infrastructure.ai

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.BoundingBoxDto
import com.canvais.api.dto.request.GesturePointDto
import com.canvais.domain.model.AiServiceStatus
import com.canvais.domain.model.ChatActionType
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt

/**
 * Specification verifying OllamaChatModelAdapter behavior, structured prompt generation, and fallback logic.
 */
class OllamaChatModelAdapterSpec extends SpecificationBase {

    ChatModel chatModel = Mock()
    SpatialReasoningAdapter fallbackAdapter = Mock()
    OllamaChatModelAdapter adapter

    def setup() {
        adapter = new OllamaChatModelAdapter(chatModel, fallbackAdapter, "llama3.2", "http://localhost:11434", 30)
    }

    def "generateSpatialIntent parses valid structured LLM JSON response"() {
        given: "a structured JSON reply from the local Ollama LLM"
        def jsonPayload = '''
        {
          "assistantMessage": "I have created a secure perimeter around the area.",
          "action": "CREATE",
          "objects": [
            {
              "name": "Central Perimeter",
              "entityType": "PerimeterZone",
              "geometryType": "Polygon",
              "coordinates": [
                [[8.18, 48.17], [8.22, 48.17], [8.22, 48.19], [8.18, 48.19], [8.18, 48.17]]
              ],
              "style": { "strokeColor": "#E76F51", "fillColor": "#E76F5133", "strokeWidth": 2.5 },
              "confidence": 0.95
            }
          ]
        }
        '''
        def generation = new Generation(new AssistantMessage(jsonPayload))
        def chatResponse = new ChatResponse([generation])
        chatModel.call(_ as Prompt) >> chatResponse

        def bounds = new BoundingBoxDto(48.20, 48.16, 8.23, 8.17)
        def center = new GesturePointDto(48.18, 8.20)

        when: "generateSpatialIntent is called"
        def result = adapter.generateSpatialIntent("Draw a perimeter", [], [], bounds, center)

        then: "result contains parsed assistant narrative and proposed polygon entity"
        result != null
        result.actionType == ChatActionType.CREATE
        result.assistantMessage.contains("secure perimeter")
        result.entities.size() == 1
        result.entities[0].name == "Central Perimeter"
        result.entities[0].entityType == "PerimeterZone"
        result.entities[0].geometry.type == "Polygon"
    }

    def "checkHealth returns AVAILABLE when model is reachable"() {
        given: "a responsive chat model"
        chatModel.call(_ as Prompt) >> new ChatResponse([new Generation(new AssistantMessage("pong"))])

        when: "checkHealth is invoked"
        def health = adapter.checkHealth()

        then: "status is AVAILABLE"
        health.status == AiServiceStatus.AVAILABLE
        health.activeModel == "llama3.2"
        health.endpoint == "http://localhost:11434"
    }
}
