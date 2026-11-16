package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.controller.ChatController
import com.canvais.api.dto.request.CreateChatSessionRequest
import com.canvais.api.dto.request.SendChatMessageRequest
import com.canvais.api.dto.response.AiHealthResponse
import com.canvais.api.dto.response.ChatMessageDto
import com.canvais.api.dto.response.ChatMessageTurnResponse
import com.canvais.api.dto.response.ChatSessionDetailResponse
import com.canvais.api.dto.response.ChatSessionResponse
import com.canvais.domain.model.AiServiceStatus
import com.canvais.domain.model.ChatActionType
import com.canvais.domain.model.ChatRole
import com.canvais.service.ChatService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import java.time.Instant

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * WebMvc unit test specification verifying ChatController endpoints.
 */
class ChatControllerSpec extends SpecificationBase {

    MockMvc mockMvc
    ChatService chatService = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        def controller = new ChatController(chatService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "POST /api/v1/chat/sessions initializes a chat session"() {
        given: "a valid create session request"
        def mapSessionId = UUID.randomUUID()
        def chatSessionId = UUID.randomUUID()
        def targetLayerId = UUID.randomUUID()
        def request = new CreateChatSessionRequest(mapSessionId, "Operation Alpha")
        def response = new ChatSessionResponse(chatSessionId, mapSessionId, targetLayerId, "Operation Alpha", "llama3.2", Instant.now())
        chatService.getOrCreateSession(_ as CreateChatSessionRequest) >> response

        expect:
        mockMvc.perform(post("/api/v1/chat/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(chatSessionId.toString()))
                .andExpect(jsonPath('$.title').value("Operation Alpha"))
    }

    def "POST /api/v1/chat/sessions/{id}/messages executes prompt"() {
        given: "a prompt request"
        def chatSessionId = UUID.randomUUID()
        def targetLayerId = UUID.randomUUID()
        def request = new SendChatMessageRequest("Draw perimeter", null, null, null)
        def userMsg = new ChatMessageDto(UUID.randomUUID(), ChatRole.USER, "Draw perimeter", null, ChatActionType.NONE, Instant.now())
        def assistantMsg = new ChatMessageDto(UUID.randomUUID(), ChatRole.ASSISTANT, "Drawn perimeter", null, ChatActionType.CREATE, Instant.now())
        def turnResponse = new ChatMessageTurnResponse(userMsg, assistantMsg, ChatActionType.CREATE, targetLayerId, [])
        chatService.sendMessage(chatSessionId, _ as SendChatMessageRequest) >> turnResponse

        expect:
        mockMvc.perform(post("/api/v1/chat/sessions/${chatSessionId}/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.actionType').value("CREATE"))
                .andExpect(jsonPath('$.assistantMessage.content').value("Drawn perimeter"))
    }

    def "GET /api/v1/chat/health reports AI availability"() {
        given: "a healthy AI service status"
        chatService.checkHealth() >> new AiHealthResponse(AiServiceStatus.AVAILABLE, "llama3.2", "http://localhost:11434", 35L, "Ready")

        expect:
        mockMvc.perform(get("/api/v1/chat/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.status').value("AVAILABLE"))
                .andExpect(jsonPath('$.activeModel').value("llama3.2"))
    }
}
