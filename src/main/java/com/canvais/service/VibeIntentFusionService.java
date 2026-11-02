package com.canvais.service;

import com.canvais.api.dto.request.AcceptProposalRequest;
import com.canvais.api.dto.request.CreateEntityRequest;
import com.canvais.api.dto.request.VibeIntentProposeRequest;
import com.canvais.api.dto.response.ProposedEntityDto;
import com.canvais.api.dto.response.VibeIntentProposalResponse;
import com.canvais.domain.model.ProposalStatus;
import com.canvais.domain.model.VibeIntentProposal;
import com.canvais.domain.repository.VibeIntentProposalRepository;
import com.canvais.infrastructure.ai.SpatialReasoningAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service orchestrating multimodal vibe processing, candidate proposal generation, and acceptance commit.
 */
@Service
@Transactional
public class VibeIntentFusionService {

    private final VibeIntentProposalRepository proposalRepository;
    private final SpatialReasoningAdapter spatialReasoningAdapter;
    private final GraphicalEntityService entityService;

    public VibeIntentFusionService(VibeIntentProposalRepository proposalRepository,
                                   SpatialReasoningAdapter spatialReasoningAdapter,
                                   GraphicalEntityService entityService) {
        this.proposalRepository = proposalRepository;
        this.spatialReasoningAdapter = spatialReasoningAdapter;
        this.entityService = entityService;
    }

    /**
     * Synthesizes a new spatial proposal based on the multimodal request.
     *
     * @param request multimodal vibe and gesture input
     * @return proposal response with candidate entities
     */
    public VibeIntentProposalResponse generateProposal(VibeIntentProposeRequest request) {
        List<ProposedEntityDto> candidates = spatialReasoningAdapter.synthesizeEntities(
                request.getTextVibe(),
                request.getGestureStrokes(),
                request.getViewportBounds()
        );

        String summary = "Synthesized " + candidates.size() + " vector entities matching vibe: \""
                + request.getTextVibe() + "\"";

        VibeIntentProposal proposal = new VibeIntentProposal(request.getTargetLayerId(), summary);
        proposal.setProposedEntities(candidates);

        VibeIntentProposal saved = proposalRepository.save(proposal);

        VibeIntentProposalResponse response = new VibeIntentProposalResponse();
        response.setProposalId(saved.getProposalId());
        response.setTargetLayerId(saved.getTargetLayerId());
        response.setInterpretedSummary(saved.getInterpretedSummary());
        response.setProposedEntities(saved.getProposedEntities());
        response.setStatus(saved.getStatus());

        return response;
    }

    /**
     * Accepts and commits proposed entities into the target layer.
     *
     * @param proposalId proposal UUID
     * @param request optional selection parameters
     * @return list of committed entity IDs
     */
    public List<UUID> acceptProposal(UUID proposalId, AcceptProposalRequest request) {
        VibeIntentProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NoSuchElementException("Proposal not found with ID: " + proposalId));

        List<ProposedEntityDto> candidates = proposal.getProposedEntities();
        List<UUID> committedIds = new ArrayList<>();

        for (ProposedEntityDto candidate : candidates) {
            // If specific tempIds were provided, filter accordingly
            if (request != null && request.getSelectedTempIds() != null && !request.getSelectedTempIds().isEmpty()) {
                if (!request.getSelectedTempIds().contains(candidate.getTempId())) {
                    continue;
                }
            }

            CreateEntityRequest createRequest = new CreateEntityRequest();
            createRequest.setName(candidate.getName());
            createRequest.setEntityType(candidate.getEntityType());
            createRequest.setGeometry(candidate.getGeometry());
            createRequest.setStyle(candidate.getStyle());
            createRequest.setSemanticAttributes(candidate.getSemanticAttributes());

            var committed = entityService.createEntity(proposal.getTargetLayerId(), createRequest);
            committedIds.add(committed.getId());
        }

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalRepository.save(proposal);

        return committedIds;
    }
}
