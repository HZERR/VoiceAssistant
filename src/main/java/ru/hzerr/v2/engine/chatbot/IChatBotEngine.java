package ru.hzerr.v2.engine.chatbot;

import ru.hzerr.v2.exception.ProcessingException;

public interface IChatBotEngine<T> {

    T process(ChatBotProcessingConfiguration chatBotProcessingConfiguration) throws ProcessingException;
}
