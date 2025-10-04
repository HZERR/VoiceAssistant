package ru.hzerr.deprecated.chat;

import ru.hzerr.deprecated.processor.AssistantCommand;

import java.util.List;

public class AssistantResponse {

    private String speak;
    private List<AssistantCommand> commands;

    public AssistantResponse() {
    }

    public String getSpeak() {
        return speak;
    }

    public void setSpeak(String speak) {
        this.speak = speak;
    }

    public List<AssistantCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<AssistantCommand> commands) {
        this.commands = commands;
    }
}
