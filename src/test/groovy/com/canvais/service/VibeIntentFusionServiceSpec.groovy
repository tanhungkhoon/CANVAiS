package com.canvais.service

import com.canvais.SpecificationBase
import com.canvais.api.dto.request.AcceptProposalRequest
import com.canvais.api.dto.request.CreateEntityRequest
import com.canvais.api.dto.request.VibeIntentProposeRequest
import com.canvais.api.dto.response.GraphicalEntityResponse
import com.canvais.api.dto.response.ProposedEntityDto
import com.canvais.domain.model.ProposalStatus
import com.canvais.domain.model.VibeIntentProposal
import com.canvais.domain.repository.VibeIntentProposalRepository
import com.canvais.infrastructure.ai.SpatialReasoningAdapter

/**
 * Spock behavioral specification for VibeIntentFusionService.
 */
class VibeIntentFusionServiceSpec extends SpecificationBase {

    VibeIntentProposalRepository proposalRepository = Mock()
    SpatialReasoningAdapter spatialReasoningAdapter = Mock()
    GraphicalEntityService entityService = Mock()
    VibeIntentFusionService fusionService

    def setup() {
        fusionService = new VibeIntentFusionService(proposalRepository, spatialReasoningAdapter, entityService)
    }

    def "should generate spatial proposal from request and save entity"() {
        given: "a valid propose request"
        def layerId = UUID.randomUUID()
        def request = new VibeIntentProposeRequest(
                sessionId: UUID.randomUUID(),
                targetLayerId: layerId,
                textVibe: "Plot rally point near crossroads"
        )
        def candidate = new ProposedEntityDto(name: "Crossroad Rally Point", entityType: "RallyPoint")

        spatialReasoningAdapter.synthesizeEntities(request.textVibe, request.gestureStrokes, request.viewportBounds) >> [candidate]

        when: "proposal is generated"
        def response = fusionService.generateProposal(request)

        then: "proposal is persisted and returned"
        1 * proposalRepository.save(_ as VibeIntentProposal) >> { VibeIntentProposal p ->
            p.proposalId = UUID.randomUUID()
            return p
        }
        response.targetLayerId == layerId
        response.status == ProposalStatus.PENDING
        response.proposedEntities.size() == 1
    }

    def "should accept proposal and commit candidate entities to layer"() {
        given: "an existing pending proposal"
        def proposalId = UUID.randomUUID()
        def layerId = UUID.randomUUID()
        def candidate = new ProposedEntityDto(name: "Proposed Point", entityType: "ObservationPoint")
        candidate.geometry = [type: "Point", coordinates: [8.20, 48.18]]

        def proposal = new VibeIntentProposal(layerId, "Test summary")
        proposal.proposalId = proposalId
        proposal.proposedEntities = [candidate]

        proposalRepository.findById(proposalId) >> Optional.of(proposal)

        def committedEntityId = UUID.randomUUID()
        entityService.createEntity(layerId, _ as CreateEntityRequest) >> new GraphicalEntityResponse(id: committedEntityId)

        when: "proposal is accepted"
        def committedIds = fusionService.acceptProposal(proposalId, new AcceptProposalRequest())

        then: "proposal status transitions to ACCEPTED"
        committedIds == [committedEntityId]
        proposal.status == ProposalStatus.ACCEPTED
        1 * proposalRepository.save(proposal)
    }
}
