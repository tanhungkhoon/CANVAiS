package com.canvais.domain.repository;

import com.canvais.domain.model.ChatEntityAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ChatEntityAction entities.
 */
@Repository
public interface ChatEntityActionRepository extends JpaRepository<ChatEntityAction, UUID> {

    /**
     * Finds entity actions executed for a specific message turn.
     *
     * @param messageId unique identifier of the chat message
     * @return list of actions executed in that turn
     */
    List<ChatEntityAction> findByMessageId(UUID messageId);

    /**
     * Finds entity actions executed against a specific graphical entity.
     *
     * @param entityId unique identifier of the graphical entity
     * @return list of actions affecting the entity
     */
    List<ChatEntityAction> findByEntityId(UUID entityId);
}
