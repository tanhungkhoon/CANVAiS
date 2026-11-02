package com.canvais.api.dto.response;

import com.canvais.domain.model.ProposalStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response payload carrying candidate entities for user review.
 */
public class VibeIntentProposalResponse {

    private UUID proposalId;
    private UUID targetLayerId;
    private String interpretedSummary;
    private List<ProposedEntityDto> proposedEntities = new ArrayList<>();
    private ProposalStatus status;

    public UUID getProposalId() {
        return proposalId;
    }

    public void setProposalId(UUID proposalId) {
        this.proposalId = proposalId;
    }

    public UUID getTargetLayerId() {
        return targetLayerId;
    }

    public void setTargetLayerId(UUID targetLayerId) {
        this.targetLayerId = targetLayerId;
    }

    public String getInterpretedSummary() {
        return interpretedSummary;
    }

    public void setInterpretedSummary(String interpretedSummary) {
        this.interpretedSummary = interpretedSummary;
    }

    public List<ProposedEntityDto> getProposedEntities() {
        return proposedEntities;
    }

    public void setProposedEntities(List<ProposedEntityDto> proposedEntities) {
        this.proposedEntities = proposedEntities;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }
}
