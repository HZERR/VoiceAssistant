package ru.hzerr.v2.format.v1;

import java.util.List;

public class BotAction {

    private String command;
    private List<String> args;

    public BotAction() {
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
