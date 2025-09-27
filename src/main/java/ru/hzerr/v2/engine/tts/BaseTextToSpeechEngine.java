package ru.hzerr.v2.engine.tts;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.v2.exception.ProcessingException;

import java.net.http.HttpClient;

public abstract class BaseTextToSpeechEngine implements ITextToSpeechEngine {

    private static final Logger log = LoggerFactory.getLogger(BaseTextToSpeechEngine.class);
    protected final HttpClient httpClient;

    protected BaseTextToSpeechEngine() {
        this.httpClient = HttpClient.newHttpClient();
    }
    protected BaseTextToSpeechEngine(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @PostConstruct
    private void initialize() throws Exception {
        log.debug("📦 Инициализация модуля синтеза речи...");
        long startTime = System.currentTimeMillis();
        onInitialize();
        log.debug("✅ Модуль синтеза речи '{}' загружен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    @Override
    public byte[] synthesize(String input) throws ProcessingException {
        log.debug("🧠 Синтез речи начат (длина текста: {})...", input.length());
        long startTime = System.currentTimeMillis();
        try {
            return onSynthesize(input);
        } catch (Exception e) {
            log.error("❌ Ошибка при синтезе речи", e);
            throw new ProcessingException(e.getMessage(), e);
        } finally {
            log.debug("✅ Распознавание завершено за {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @PreDestroy
    private void destroy() throws Exception {
        log.debug("📦 Завершение работы модуля синтеза речи...");
        long startTime = System.currentTimeMillis();
        onDestroy();
        httpClient.close();
        log.debug("✅ Модуль синтеза речи '{}' успешно завершен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    protected abstract String getModuleName();
    protected abstract void onInitialize() throws Exception;
    protected abstract byte[] onSynthesize(String input) throws Exception;
    protected abstract void onDestroy() throws Exception;
}
