package ru.hzerr.v2.engine.record;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.utils.DaemonThreadFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseSpeechRecordEngine implements ISpeechRecordEngine {

    private static final Logger log = LoggerFactory.getLogger(BaseSpeechRecordEngine.class);
    protected final AtomicBoolean active = new AtomicBoolean(false);
    private Thread recordThread;

    @PostConstruct
    private void init() throws Exception {
        log.debug("📦 Инициализация модуля записи речи...");
        long startTime = System.currentTimeMillis();
        onInitialize();
        log.debug("✅ Модуль записи речи '{}' загружен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    @Override
    public void start() {
        if (active.compareAndSet(false, true)) {
            log.debug("▶️ Запись началась...");
            onStart();
            recordThread = DaemonThreadFactory.newFactory("Audio Record Thread").newThread(this::record);
            recordThread.start();
        }
    }

    @Override
    public void stop() {
        if (active.compareAndSet(true, false)) {
            log.debug("✅ Запись завершена");
            onStop();
            try {
                recordThread.join();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @PreDestroy
    private void destroy() throws Exception {
        log.debug("📦 Завершение работы модуля записи речи...");
        long startTime = System.currentTimeMillis();
        onDestroy();
        log.debug("✅ Модуль записи речи '{}' успешно завершен за {}ms", getModuleName(), System.currentTimeMillis() - startTime);
    }

    protected abstract String getModuleName();
    protected abstract void onInitialize() throws Exception;
    protected abstract void onStart();
    protected abstract void record();
    protected abstract void onStop();
    protected abstract void onDestroy() throws Exception;
}
