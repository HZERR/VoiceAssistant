package ru.hzerr.v2.engine.play;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hzerr.utils.DaemonThreadFactory;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AudioPlaybackEngine extends BaseAudioPlaybackEngine {

    private static final AudioFormat format = new AudioFormat(48000, 16, 1, true, false);
    private static final ExecutorService playbackQueue = Executors.newSingleThreadExecutor(new DaemonThreadFactory("Audio Playback Thread"));
    private static final Logger log = LoggerFactory.getLogger(AudioPlaybackEngine.class);

    @Override
    protected String getModuleName() {
        return "Base";
    }

    @Override
    protected void onInitialize() throws Exception {

    }

    @Override
    protected void onPlay(byte[] input) throws Exception {
        playbackQueue.submit(() -> {
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                line.write(input, 0, input.length);
                line.drain();
            } catch (LineUnavailableException e) {
                log.error("❌ Ошибка при воспроизведении речи", e);
            }
        });
    }

    @Override
    protected void onDestroy() throws Exception {
        playbackQueue.close();
    }
}
