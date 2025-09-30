package ru.hzerr.v2.format.v1;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ChatBotInstruction {

    private String speak;
    private List<BotAction> commands;

    public ChatBotInstruction() {
    }

    public String getSpeak() {
        return speak;
    }

    public void setSpeak(String speak) {
        this.speak = speak;
    }

    public List<BotAction> getCommands() {
        return commands;
    }

    public void setCommands(List<BotAction> commands) {
        this.commands = commands;
    }

    public boolean hasSpeech() {
        return StringUtils.isNotEmpty(speak);
    }

    public boolean hasActions() {
        return !commands.isEmpty();
    }
}
