package com.canvais.domain.model;

import com.canvais.api.dto.response.ProposedEntityDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing an AI-generated proposal containing candidate map entities.
 */
@Entity
@Table(name = "vibe_intent_proposals")
public class VibeIntentProposal {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @NotNull
    private UUID proposalId;

    @NotNull
    @Column(nullable = false)
    private UUID targetLayerId;

    @Column(length = 500)
    private String interpretedSummary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String proposedEntitiesJson;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    @NotNull
    @Column(nullable = false)
    private Instant createdAt;

    public VibeIntentProposal() {
        this.proposalId = UUID.randomUUID();
        this.status = ProposalStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public VibeIntentProposal(UUID targetLayerId, String interpretedSummary) {
        this();
        this.targetLayerId = targetLayerId;
        this.interpretedSummary = interpretedSummary;
    }

    @PrePersist
    protected void onCreate() {
        if (this.proposalId == null) {
            this.proposalId = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = ProposalStatus.PENDING;
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

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
        if (proposedEntitiesJson == null || proposedEntitiesJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(proposedEntitiesJson, new TypeReference<List<ProposedEntityDto>>() { });
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setProposedEntities(List<ProposedEntityDto> entities) {
        if (entities == null || entities.isEmpty()) {
            this.proposedEntitiesJson = null;
        } else {
            try {
                this.proposedEntitiesJson = OBJECT_MAPPER.writeValueAsString(entities);
            } catch (JsonProcessingException e) {
                this.proposedEntitiesJson = null;
            }
        }
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
