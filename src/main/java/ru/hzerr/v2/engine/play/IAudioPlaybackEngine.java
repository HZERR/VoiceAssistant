package ru.hzerr.v2.engine.play;

import ru.hzerr.v2.exception.ProcessingException;

public interface IAudioPlaybackEngine {

    void play(byte[] input) throws ProcessingException;
}
