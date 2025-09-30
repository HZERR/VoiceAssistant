package ru.hzerr.v2.engine.chatbot;

public class ChatBotProcessingConfiguration {

    private String message;
    private ChatBotRole role;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setRole(ChatBotRole role) {
        this.role = role;
    }

    public ChatBotRole getRole() {
        return role;
    }
}
