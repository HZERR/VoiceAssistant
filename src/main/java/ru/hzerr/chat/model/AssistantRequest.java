package ru.hzerr.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssistantRequest {

    private String modelUri;
    private AssistantOptions assistantOptions;
    private List<Message> messages;

    public AssistantRequest(String modelUri, AssistantOptions assistantOptions, List<Message> messages) {
        this.modelUri = modelUri;
        this.assistantOptions = assistantOptions;
        this.messages = messages;
    }

    public String getModelUri() {
        return modelUri;
    }

    public void setModelUri(String modelUri) {
        this.modelUri = modelUri;
    }

    public AssistantOptions getCompletionOptions() {
        return assistantOptions;
    }

    public void setCompletionOptions(AssistantOptions assistantOptions) {
        this.assistantOptions = assistantOptions;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

}
