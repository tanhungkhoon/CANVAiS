package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.controller.ChatController
import com.canvais.api.dto.response.ClearLayerResponse
import com.canvais.service.ChatService
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Specification verifying DELETE /api/v1/chat/sessions/{chatSessionId}/layer endpoint.
 */
class ChatLayerControllerSpec extends SpecificationBase {

    MockMvc mockMvc
    ChatService chatService = Mock()

    def setup() {
        def controller = new ChatController(chatService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "DELETE /api/v1/chat/sessions/{id}/layer returns ClearLayerResponse"() {
        given: "a session ID and cleared layer response"
        def chatSessionId = UUID.randomUUID()
        def layerId = UUID.randomUUID()
        chatService.clearOverlayLayer(chatSessionId) >> new ClearLayerResponse(layerId, 4)

        expect:
        mockMvc.perform(delete("/api/v1/chat/sessions/${chatSessionId}/layer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.layerId').value(layerId.toString()))
                .andExpect(jsonPath('$.clearedEntitiesCount').value(4))
    }
}
