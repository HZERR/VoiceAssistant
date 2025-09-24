package ru.hzerr.processor.impl;

import ru.hzerr.processor.AssistantCommand;
import ru.hzerr.processor.Command;
import ru.hzerr.processor.IProcessor;

@Command("getProdMemory")
public class ServerMemoryProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        return "[КОМПЬЮТЕР] com.jcraft.jsch.JSchException: timeout: socket is not established";
    }
}
