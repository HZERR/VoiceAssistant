package ru.hzerr.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

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
    private static final Path PROGRAM_DIRECTORY = Path.of(System.getProperty("user.home"), "Voice Assistant");
    private static final Path WHISPER_DIRECTORY = PROGRAM_DIRECTORY.resolve("Whisper");
    private static final String WHISPER_PROGRAM_RELATIVE_PATH = "/ru/hzerr/stt/whisper/whisper-cublas-12.4.0-bin-x64.zip";
    private static final String WHISPER_MEDIUM_Q8_MODEL_RELATIVE_PATH = "/ru/hzerr/stt/whisper/ggml-medium-q8_0.bin";
    private static final String WHISPER_MEDIUM_Q8_MODEL_NAME = "ggml-medium-q8_0.bin";

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

    public Path getProgramDirectory() {
        return PROGRAM_DIRECTORY;
    }

    public Path getWhisperDirectory() {
        return WHISPER_DIRECTORY;
    }

    public String getWhisperProgramRelativePath() {
        return WHISPER_PROGRAM_RELATIVE_PATH;
    }

    public String getWhisperMediumQ8ModelRelativePath() {
        return WHISPER_MEDIUM_Q8_MODEL_RELATIVE_PATH;
    }

    public String getWhisperMediumQ8ModelName() {
        return WHISPER_MEDIUM_Q8_MODEL_NAME;
    }
}
