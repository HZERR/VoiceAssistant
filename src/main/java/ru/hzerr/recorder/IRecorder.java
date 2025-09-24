package ru.hzerr.recorder;

import java.io.ByteArrayOutputStream;

public interface IRecorder {

    void start();
    void stop();
    ByteArrayOutputStream getAudioStream();
    boolean isRecording();
}
