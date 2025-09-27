package ru.hzerr.v2.engine.record;

public interface ISpeechRecordEngine {

    void start();
    void stop();
    byte[] getAudio();
    boolean isActive();
}
