package ru.hzerr.v2.engine.play;

public class AudioPlaybackEngineFactory {

    private final IAudioPlaybackEngine audioPlaybackEngine;

    public AudioPlaybackEngineFactory(AudioPlaybackEngine audioPlaybackEngine) {
        this.audioPlaybackEngine = audioPlaybackEngine;
    }

    public IAudioPlaybackEngine getEngine(AudioPlaybackConfiguration playbackConfiguration) {
        return audioPlaybackEngine;
    }
}
