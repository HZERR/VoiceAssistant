package ru.hzerr.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class YandexAssistantResponse {

    private AssistantResult result;

    public YandexAssistantResponse() {
    }

    public AssistantResult getResult() {
        return result;
    }

    public void setResult(AssistantResult result) {
        this.result = result;
    }

    public static class AssistantResult {

        private List<Alternative> alternatives;
        private Usage usage;
        private String modelVersion;

        public AssistantResult() {
        }

        public List<Alternative> getAlternatives() {
            return alternatives;
        }

        public void setAlternatives(List<Alternative> alternatives) {
            this.alternatives = alternatives;
        }

        public Usage getUsage() {
            return usage;
        }

        public void setUsage(Usage usage) {
            this.usage = usage;
        }

        public String getModelVersion() {
            return modelVersion;
        }

        public void setModelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alternative {

        private Message message;
        private String status;

        public Alternative() {
        }

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        @JsonProperty("inputTextTokens")
        private String inputTextTokens;
        @JsonProperty("completionTokens")
        private String completionTokens;
        @JsonProperty("totalTokens")
        private String totalTokens;
        private AssistantTokensDetails assistantTokensDetails;

        public Usage() {
        }

        public String getInputTextTokens() {
            return inputTextTokens;
        }

        public void setInputTextTokens(String inputTextTokens) {
            this.inputTextTokens = inputTextTokens;
        }

        public String getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(String completionTokens) {
            this.completionTokens = completionTokens;
        }

        public String getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(String totalTokens) {
            this.totalTokens = totalTokens;
        }

        public AssistantTokensDetails getCompletionTokensDetails() {
            return assistantTokensDetails;
        }

        public void setCompletionTokensDetails(AssistantTokensDetails assistantTokensDetails) {
            this.assistantTokensDetails = assistantTokensDetails;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssistantTokensDetails {

        @JsonProperty("reasoningTokens")
        private String reasoningTokens;

        public AssistantTokensDetails() {
        }

        public String getReasoningTokens() {
            return reasoningTokens;
        }

        public void setReasoningTokens(String reasoningTokens) {
            this.reasoningTokens = reasoningTokens;
        }
    }
}
