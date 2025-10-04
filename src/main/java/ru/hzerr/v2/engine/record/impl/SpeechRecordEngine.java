package ru.hzerr.v2.engine.record.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import ru.hzerr.v2.engine.record.BaseSpeechRecordEngine;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

@Component
public class SpeechRecordEngine extends BaseSpeechRecordEngine {

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000, 16, 1, true, false);
    private TargetDataLine microphone;
    private byte[] recordedAudio;

    @Override
    protected String getModuleName() {
        return "Java Base";
    }

    @Override
    protected void onInitialize() throws Exception {
        microphone = getMicrophone(AUDIO_FORMAT, "Микрофон (5- ME6S)");
        microphone.open(AUDIO_FORMAT);
    }

    @Override
    protected void onStart() {
        microphone.start();
        recordedAudio = new byte[0];
    }

    @Override
    protected void record() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        while (active.get()) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);
            if (bytesRead > 0) stream.write(buffer, 0, bytesRead);
        }

        recordedAudio = stream.toByteArray();
    }

    @Override
    protected void onStop() {
        microphone.stop();
    }

    @Override
    protected void onDestroy() throws Exception {
        microphone.stop();
        microphone.close();
    }

    @Override
    public byte[] getAudio() {
        return ObjectUtils.getIfNull(recordedAudio, new byte[0]);
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
