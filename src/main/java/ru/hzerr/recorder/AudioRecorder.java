package ru.hzerr.recorder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hzerr.utils.DaemonThreadFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AudioRecorder implements IRecorder {

    private static final Logger log = LoggerFactory.getLogger(AudioRecorder.class);
    private static final ExecutorService recorderService = Executors.newSingleThreadExecutor(DaemonThreadFactory.newFactory("Audio Recorder Thread"));
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000, 16, 1, true, false);
    private final AtomicBoolean recording = new AtomicBoolean(false);
    private TargetDataLine microphone;
    private Future<?> recorderTask;
    private ByteArrayOutputStream audioStream;

    @PostConstruct
    private void init() throws Exception {
        microphone = getMicrophone(AUDIO_FORMAT, "Микрофон (4- ME6S)");
        microphone.open(AUDIO_FORMAT);
    }

    @PreDestroy
    private void destroy() {
        recorderService.close();
        log.debug("✅ Модуль записи звука успешно завершил работу");
    }

    @Override
    public void start() {
        if (recording.compareAndSet(false, true)) {
            log.debug("▶️ Запись началась...");
            try {
                audioStream = new ByteArrayOutputStream();
                microphone.start();

                // Возможно стоит в методе stop дожидаться завершения задачи? p.s пока работает норм
                recorderTask = recorderService.submit(() -> {
                    byte[] buffer = new byte[4096];
                    try {
                        while (recording.get()) {
                            int bytesRead = microphone.read(buffer, 0, buffer.length);
                            if (bytesRead > 0) {
                                audioStream.write(buffer, 0, bytesRead);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Ошибка при записи аудио: ", e);
                    }
                });
            } catch (Exception e) {
                log.error("Ошибка при запуске записи: ", e);
            }
        }
    }

    @Override
    public void stop() {
        if (recording.compareAndSet(true, false)) {
            try {
                recorderTask.get();
            } catch (InterruptedException | ExecutionException e) { log.error(e.getMessage(), e); }

            microphone.stop();
            microphone.flush();
            log.debug("⏹ Запись остановлена.");
        }
    }

    @Override
    public ByteArrayOutputStream getAudioStream() {
        return audioStream;
    }

    @Override
    public boolean isRecording() {
        return recording.get();
    }

    private TargetDataLine getMicrophone(AudioFormat format, String deviceName) throws LineUnavailableException {
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().contains(deviceName)) {
                Mixer mixer = AudioSystem.getMixer(info);
                Line.Info[] lines = mixer.getTargetLineInfo();
                for (Line.Info lineInfo : lines) {
                    if (TargetDataLine.class.isAssignableFrom(lineInfo.getLineClass())) {
                        if (AudioSystem.isLineSupported(lineInfo)) {
                            TargetDataLine line = (TargetDataLine) mixer.getLine(lineInfo);
                            line.open(format);
                            return line;
                        }
                    }
                }
            }
        }
        throw new LineUnavailableException("❌ Не найден подходящий микрофон с именем: " + deviceName);
    }

}
