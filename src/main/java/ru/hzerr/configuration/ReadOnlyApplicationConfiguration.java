package ru.hzerr.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("SpellCheckingInspection")
public class ReadOnlyApplicationConfiguration implements IReadOnlyConfiguration {

    @Value("${module.recognizer.vosk.model.ru.v0.22}") // 1.5GB
    private String voskModel22Directory;
    @Value("${module.recognizer.vosk.model.small.ru.v0.22}") // 45MB
    private String voskModelSmall22Directory;
    @Value("${module.recognizer.vosk.model.ru.v0.42}") // 1.8GB
    private String voskModel42Directory;
    @Value("${module.assistant.yandex.secret.key}")
    private String yandexCloudSecretKey;
    @Value("${module.assistant.open.ai.base.url}")
    private String openAIAssistantBaseUrl;
    @Value("${module.assistant.open.ai.api.key}")
    private String openAIAssistantApiKey;
    @Value("${service.time.world.api.key}")
    private String timeWorldApiKey;

    public ReadOnlyApplicationConfiguration() {
    }

    public String getVoskModel22Directory() {
        return voskModel22Directory;
    }

    public String getVoskModelSmall22Directory() {
        return voskModelSmall22Directory;
    }

    public String getVoskModel42Directory() {
        return voskModel42Directory;
    }

    public String getYandexSecretKey() {
        return yandexCloudSecretKey;
    }

    public String getOpenAIAssistantBaseUrl() {
        return openAIAssistantBaseUrl;
    }

    public String getOpenAIAssistantApiKey() {
        return openAIAssistantApiKey;
    }

    public String getTimeWorldApiKey() {
        return timeWorldApiKey;
    }
}
