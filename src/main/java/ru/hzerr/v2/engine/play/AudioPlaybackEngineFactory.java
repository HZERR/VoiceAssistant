package ru.hzerr.v2.engine.play;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AudioPlaybackEngineFactory {

    private final IAudioPlaybackEngine audioPlaybackEngine;

    @Autowired
    public AudioPlaybackEngineFactory(AudioPlaybackEngine audioPlaybackEngine) {
        this.audioPlaybackEngine = audioPlaybackEngine;
    }

    public IAudioPlaybackEngine getEngine(AudioPlaybackConfiguration playbackConfiguration) {
        return audioPlaybackEngine;
    }
}
