package com.canvais.domain.repository;

import com.canvais.domain.model.MapSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for MapSession persistence.
 */
@Repository
public interface MapSessionRepository extends JpaRepository<MapSession, UUID> {
}
