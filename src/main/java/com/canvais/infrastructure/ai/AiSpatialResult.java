package com.canvais.infrastructure.ai;

import com.canvais.api.dto.response.ProposedEntityDto;
import com.canvais.domain.model.ChatActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Result object produced by the AI model client containing conversational reply and spatial proposed entities.
 */
public class AiSpatialResult {

    private String assistantMessage;
    private ChatActionType actionType = ChatActionType.NONE;
    private UUID targetEntityId;
    private List<ProposedEntityDto> entities = new ArrayList<>();

    public AiSpatialResult() {
    }

    public AiSpatialResult(String assistantMessage, ChatActionType actionType, UUID targetEntityId, List<ProposedEntityDto> entities) {
        this.assistantMessage = assistantMessage;
        this.actionType = actionType != null ? actionType : ChatActionType.NONE;
        this.targetEntityId = targetEntityId;
        if (entities != null) {
            this.entities = entities;
        }
    }

    public String getAssistantMessage() {
        return assistantMessage;
    }

    public void setAssistantMessage(String assistantMessage) {
        this.assistantMessage = assistantMessage;
    }

    public ChatActionType getActionType() {
        return actionType;
    }

    public void setActionType(ChatActionType actionType) {
        this.actionType = actionType;
    }

    public UUID getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(UUID targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public List<ProposedEntityDto> getEntities() {
        return entities;
    }

    public void setEntities(List<ProposedEntityDto> entities) {
        this.entities = entities;
    }
}
