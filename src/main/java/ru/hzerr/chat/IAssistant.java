package ru.hzerr.chat;

public interface IAssistant {

    String process(String message, AssistantProcessingOptions assistantProcessingOptions);
}
