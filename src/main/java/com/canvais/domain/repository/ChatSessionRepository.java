package com.canvais.domain.repository;

import com.canvais.domain.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ChatSession entities.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    /**
     * Finds the most recent chat session associated with a map session.
     *
     * @param mapSessionId unique identifier of the map session
     * @return optional chat session
     */
    Optional<ChatSession> findFirstByMapSessionIdOrderByCreatedAtDesc(UUID mapSessionId);

    /**
     * Finds all chat sessions for a given map session.
     *
     * @param mapSessionId unique identifier of the map session
     * @return list of chat sessions
     */
    List<ChatSession> findByMapSessionId(UUID mapSessionId);
}
