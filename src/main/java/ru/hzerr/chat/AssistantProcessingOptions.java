package ru.hzerr.chat;

public class AssistantProcessingOptions {

    private String role;

    public AssistantProcessingOptions(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
