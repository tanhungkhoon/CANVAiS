package com.canvais.domain.repository;

import com.canvais.domain.model.VibeIntentProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for VibeIntentProposal persistence.
 */
@Repository
public interface VibeIntentProposalRepository extends JpaRepository<VibeIntentProposal, UUID> {
}
