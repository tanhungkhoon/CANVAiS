package com.canvais.api.dto.response;

import com.canvais.domain.model.ChatActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response payload returning the results of a chat interaction turn, including assistant narrative and affected entities.
 */
public class ChatMessageTurnResponse {

    private ChatMessageDto userMessage;
    private ChatMessageDto assistantMessage;
    private ChatActionType actionType;
    private UUID targetLayerId;
    private List<GraphicalEntityResponse> affectedEntities = new ArrayList<>();

    public ChatMessageTurnResponse() {
    }

    public ChatMessageTurnResponse(ChatMessageDto userMessage, ChatMessageDto assistantMessage,
                                  ChatActionType actionType, UUID targetLayerId,
                                  List<GraphicalEntityResponse> affectedEntities) {
        this.userMessage = userMessage;
        this.assistantMessage = assistantMessage;
        this.actionType = actionType;
        this.targetLayerId = targetLayerId;
        if (affectedEntities != null) {
            this.affectedEntities = affectedEntities;
        }
    }

    public ChatMessageDto getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(ChatMessageDto userMessage) {
        this.userMessage = userMessage;
    }

    public ChatMessageDto getAssistantMessage() {
        return assistantMessage;
    }

    public void setAssistantMessage(ChatMessageDto assistantMessage) {
        this.assistantMessage = assistantMessage;
    }

    public ChatActionType getActionType() {
        return actionType;
    }

    public void setActionType(ChatActionType actionType) {
        this.actionType = actionType;
    }

    public UUID getTargetLayerId() {
        return targetLayerId;
    }

    public void setTargetLayerId(UUID targetLayerId) {
        this.targetLayerId = targetLayerId;
    }

    public List<GraphicalEntityResponse> getAffectedEntities() {
        return affectedEntities;
    }

    public void setAffectedEntities(List<GraphicalEntityResponse> affectedEntities) {
        this.affectedEntities = affectedEntities;
    }
}
