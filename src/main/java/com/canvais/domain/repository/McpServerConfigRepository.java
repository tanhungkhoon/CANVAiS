package com.canvais.domain.repository;

import com.canvais.domain.model.McpServerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for McpServerConfig persistence.
 */
@Repository
public interface McpServerConfigRepository extends JpaRepository<McpServerConfig, String> {
}
