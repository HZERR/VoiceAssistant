package ru.hzerr.v2.engine.stt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.v2.exception.ProcessingException;

public abstract class BaseSpeechToTextEngine implements ISpeechToTextEngine {

    private static final Logger log = LoggerFactory.getLogger(BaseSpeechToTextEngine.class);

    @PostConstruct
    private void initialize() throws Exception {
        log.debug("📦 Инициализация модуля распознавания речи...");
        long startTime = System.currentTimeMillis();
        onInitialize();
        log.debug("✅ Модуль распознавания речи '{}' загружен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    @Override
    public String recognize(byte[] input) throws ProcessingException {
        log.debug("🧠 Распознавание речи началось (размер: {} bytes)...", input.length);
        long startTime = System.currentTimeMillis();
        try {
            return onRecognize(input);
        } catch (Exception e) {
            log.error("❌ Ошибка при распознавании речи", e);
            throw new ProcessingException(e.getMessage(), e);
        } finally {
            log.debug("✅ Распознавание завершено за {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @PreDestroy
    private void destroy() throws Exception {
        log.debug("📦 Завершение работы модуля распознавания речи...");
        long startTime = System.currentTimeMillis();
        onDestroy();
        log.debug("✅ Модуль распознавания речи '{}' успешно завершен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    protected abstract String getModuleName();
    protected abstract void onInitialize() throws Exception;
    protected abstract String onRecognize(byte[] input) throws Exception;
    protected abstract void onDestroy() throws Exception;
}
