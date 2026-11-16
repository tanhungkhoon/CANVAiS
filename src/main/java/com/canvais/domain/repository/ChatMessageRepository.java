package com.canvais.domain.repository;

import com.canvais.domain.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ChatMessage entities.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Retrieves all messages for a specific chat session ordered chronologically.
     *
     * @param chatSessionId unique identifier of the chat session
     * @return chronological list of messages
     */
    List<ChatMessage> findByChatSessionIdOrderByTimestampAsc(UUID chatSessionId);
}
