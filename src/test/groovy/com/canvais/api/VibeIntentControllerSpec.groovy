package com.canvais.api

import com.canvais.SpecificationBase
import com.canvais.api.controller.VibeIntentController
import com.canvais.api.dto.request.VibeIntentProposeRequest
import com.canvais.api.dto.response.VibeIntentProposalResponse
import com.canvais.domain.model.ProposalStatus
import com.canvais.infrastructure.ai.SpeechTranscriptionAdapter
import com.canvais.service.VibeIntentFusionService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * WebMvc unit test specification verifying VibeIntentController endpoints.
 */
class VibeIntentControllerSpec extends SpecificationBase {

    MockMvc mockMvc
    VibeIntentFusionService fusionService = Mock()
    SpeechTranscriptionAdapter speechTranscriptionAdapter = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        def controller = new VibeIntentController(fusionService, speechTranscriptionAdapter)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    def "POST /api/v1/vibe/propose should return proposal response"() {
        given: "a propose request"
        def sessionId = UUID.randomUUID()
        def layerId = UUID.randomUUID()
        def proposalId = UUID.randomUUID()

        def request = new VibeIntentProposeRequest(
                sessionId: sessionId,
                targetLayerId: layerId,
                textVibe: "Establish observation perimeter"
        )
        def response = new VibeIntentProposalResponse(
                proposalId: proposalId,
                targetLayerId: layerId,
                interpretedSummary: "Generated 1 perimeter",
                status: ProposalStatus.PENDING
        )

        fusionService.generateProposal(_ as VibeIntentProposeRequest) >> response

        when: "propose endpoint is invoked"
        def result = mockMvc.perform(post("/api/v1/vibe/propose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "status is 200 OK and fields match"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.proposalId').value(proposalId.toString()))
                .andExpect(jsonPath('$.interpretedSummary').value("Generated 1 perimeter"))
                .andExpect(jsonPath('$.status').value("PENDING"))
    }
}
