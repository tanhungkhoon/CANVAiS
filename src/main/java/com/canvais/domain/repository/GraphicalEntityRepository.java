package com.canvais.domain.repository;

import com.canvais.domain.model.GraphicalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for GraphicalEntity persistence.
 */
@Repository
public interface GraphicalEntityRepository extends JpaRepository<GraphicalEntity, UUID> {

    /**
     * Finds all entities located within a specific map layer.
     *
     * @param layerId layer UUID
     * @return list of entities
     */
    List<GraphicalEntity> findByLayerId(UUID layerId);
}
