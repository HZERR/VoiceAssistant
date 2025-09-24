package ru.hzerr.processor;

import java.util.List;

public class AssistantCommand {

    private String command;
    private List<String> args;

    public AssistantCommand() {
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}
