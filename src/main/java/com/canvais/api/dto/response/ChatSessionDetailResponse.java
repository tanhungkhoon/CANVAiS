package com.canvais.api.dto.response;

import java.util.List;

/**
 * Response payload containing chat session details and message history.
 */
public class ChatSessionDetailResponse {

    private ChatSessionResponse session;
    private List<ChatMessageDto> messages;

    public ChatSessionDetailResponse() {
    }

    public ChatSessionDetailResponse(ChatSessionResponse session, List<ChatMessageDto> messages) {
        this.session = session;
        this.messages = messages;
    }

    public ChatSessionResponse getSession() {
        return session;
    }

    public void setSession(ChatSessionResponse session) {
        this.session = session;
    }

    public List<ChatMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDto> messages) {
        this.messages = messages;
    }
}
