package ru.hzerr.v2.command.processor;

import ru.hzerr.v2.exception.ProcessingException;
import ru.hzerr.v2.format.v1.BotAction;

public interface IProcessor {

    void init() throws Exception;
    String process(BotAction action) throws ProcessingException;
    void destroy() throws Exception;
}
