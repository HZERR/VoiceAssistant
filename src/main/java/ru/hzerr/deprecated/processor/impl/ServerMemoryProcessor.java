package ru.hzerr.deprecated.processor.impl;

import ru.hzerr.deprecated.processor.AssistantCommand;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.deprecated.processor.IProcessor;

//@Command("getProdMemory")
public class ServerMemoryProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        return "[КОМПЬЮТЕР] com.jcraft.jsch.JSchException: timeout: socket is not established";
    }
}
