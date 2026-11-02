package com.canvais.domain.repository;

import com.canvais.domain.model.SemanticSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for SemanticSchema persistence.
 */
@Repository
public interface SemanticSchemaRepository extends JpaRepository<SemanticSchema, UUID> {
}
