package com.canvais.domain.repository;

import com.canvais.domain.model.MapLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for MapLayer persistence.
 */
@Repository
public interface MapLayerRepository extends JpaRepository<MapLayer, UUID> {

    /**
     * Finds all layers belonging to a specific map session, ordered by z-index.
     *
     * @param sessionId map session UUID
     * @return ordered list of layers
     */
    List<MapLayer> findBySessionIdOrderByOrderIndexAsc(UUID sessionId);
}
