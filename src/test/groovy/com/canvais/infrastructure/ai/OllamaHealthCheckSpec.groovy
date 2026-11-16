package com.canvais.infrastructure.ai

import com.canvais.SpecificationBase
import com.canvais.domain.model.AiServiceStatus
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt

/**
 * Specification verifying Ollama health checking and error status reporting.
 */
class OllamaHealthCheckSpec extends SpecificationBase {

    ChatModel chatModel = Mock()
    SpatialReasoningAdapter fallbackAdapter = Mock()
    OllamaChatModelAdapter adapter

    def setup() {
        adapter = new OllamaChatModelAdapter(chatModel, fallbackAdapter, "llama3.2", "http://localhost:11434", 30)
    }

    def "checkHealth returns UNAVAILABLE with diagnostic message when chatModel throws exception"() {
        given: "a model throwing connection exception"
        chatModel.call(_ as Prompt) >> { throw new RuntimeException("Connection refused to localhost:11434") }

        when: "checkHealth is called"
        def health = adapter.checkHealth()

        then: "status is UNAVAILABLE and message describes connection failure"
        health != null
        health.status == AiServiceStatus.UNAVAILABLE
        health.activeModel == "llama3.2"
        health.message.contains("Cannot reach Ollama")
    }

    def "checkHealth returns UNAVAILABLE when chatModel is null"() {
        given: "adapter with null chatModel"
        def nullAdapter = new OllamaChatModelAdapter(null, fallbackAdapter, "llama3.2", "http://localhost:11434", 30)

        when: "checkHealth is called"
        def health = nullAdapter.checkHealth()

        then: "status is UNAVAILABLE"
        health.status == AiServiceStatus.UNAVAILABLE
        health.message.contains("not initialized")
    }
}
