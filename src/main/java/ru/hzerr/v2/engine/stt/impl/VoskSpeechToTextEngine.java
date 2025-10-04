package ru.hzerr.v2.engine.stt.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.vosk.Model;
import org.vosk.Recognizer;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.utils.JsonUtils;
import ru.hzerr.v2.engine.stt.BaseSpeechToTextEngine;

import java.io.IOException;

@Component
public class VoskSpeechToTextEngine extends BaseSpeechToTextEngine {

    private final ReadOnlyApplicationConfiguration applicationConfiguration;
    private Model model;

    public VoskSpeechToTextEngine(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    protected String getModuleName() {
        return "Vosk";
    }

    @Override
    protected void onInitialize() throws IOException {
        this.model = new Model(applicationConfiguration.getVoskModelSmall22Directory());
    }

    @Override
    protected String onRecognize(byte[] input) throws Exception {
        try (Recognizer recognizer = new Recognizer(model, 16000)) {
            recognizer.acceptWaveForm(input, input.length);

            return StringUtils.defaultString(JsonUtils.read(recognizer.getFinalResult()).path("text").asText());
        }
    }

    @Override
    protected void onDestroy() {
        model.close();
    }
}
