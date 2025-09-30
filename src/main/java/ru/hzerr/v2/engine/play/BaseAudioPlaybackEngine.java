package ru.hzerr.v2.engine.play;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.v2.exception.ProcessingException;

public abstract class BaseAudioPlaybackEngine implements IAudioPlaybackEngine {

    private static final Logger log = LoggerFactory.getLogger(BaseAudioPlaybackEngine.class);

    @PostConstruct
    private void initialize() throws Exception {
        log.debug("📦 Инициализация модуля воспроизведения речи...");
        long startTime = System.currentTimeMillis();
        onInitialize();
        log.debug("✅ Модуль воспроизведения речи '{}' загружен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    @Override
    public void play(byte[] input) throws ProcessingException {
        log.debug("🧠 Воспроизведение речи началось (размер: {} bytes)...", input.length);
        long startTime = System.currentTimeMillis();
        try {
            onPlay(input);
        } catch (Exception e) {
            log.error("❌ Ошибка при воспроизведении речи", e);
            throw new ProcessingException(e.getMessage(), e);
        } finally {
            log.debug("✅ Воспроизведение завершено за {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @PreDestroy
    private void destroy() throws Exception {
        log.debug("📦 Завершение работы модуля воспроизведения речи...");
        long startTime = System.currentTimeMillis();
        onDestroy();
        log.debug("✅ Модуль воспроизведения речи '{}' успешно завершен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    protected abstract String getModuleName();
    protected abstract void onInitialize() throws Exception;
    protected abstract void onPlay(byte[] input) throws Exception;
    protected abstract void onDestroy() throws Exception;
}
