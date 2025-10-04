package ru.hzerr.deprecated.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssistantOptions {

    private boolean stream;
    private double temperature;
    private String maxTokens;

    public AssistantOptions(boolean stream, double temperature, String maxTokens) {
        this.stream = stream;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(String maxTokens) {
        this.maxTokens = maxTokens;
    }
}
