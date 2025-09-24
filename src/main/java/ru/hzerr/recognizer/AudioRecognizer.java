package ru.hzerr.recognizer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vosk.Model;
import org.vosk.Recognizer;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.utils.JsonUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Component
public class AudioRecognizer implements IRecognizer {

    private static final Logger log = LoggerFactory.getLogger(AudioRecognizer.class);
    private final ReadOnlyApplicationConfiguration applicationConfiguration;
    private Model model;

    @Autowired
    public AudioRecognizer(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @PostConstruct
    private void init() throws Exception {
        log.debug("📦 Инициализация модели распознавания голоса...");
        long startTime = System.currentTimeMillis();
        this.model = new Model(applicationConfiguration.getVoskModelSmall22Directory());
        log.debug("✅ Модель распознавания голоса '{}' загружена за {}ms", applicationConfiguration.getVoskModelSmall22Directory(), System.currentTimeMillis() - startTime);
    }

    @PreDestroy
    private void destroy() {
        model.close();
        log.debug("✅ Модуль распознавания голоса успешно завершил работу");
    }

    @Override
    public String recognize(byte[] input) {
        long startTime = System.currentTimeMillis();
        log.debug("🧠 Распознавание голоса началось...");

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(input); Recognizer recognizer = new Recognizer(model, 16000)) {

            byte[] buffer = new byte[4096];
            StringBuilder result = new StringBuilder();

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    append(result, recognizer.getResult());
                }
            }

            append(result, recognizer.getFinalResult());
            return result.toString().trim();
        } catch (IOException e) {
            log.error("❌ Ошибка при распознавании аудио", e);
            throw new IllegalStateException("Ошибка распознавания аудио", e);
        } finally {
            log.debug("✅ Распознавание завершено за {}ms", System.currentTimeMillis() - startTime);
        }
    }

    private void append(StringBuilder output, String json) throws IOException {
        String text = JsonUtils.read(json).path("text").asText();
        if (StringUtils.isNotEmpty(text)) {
            output.append(text).append(" ");
        }
    }
}
