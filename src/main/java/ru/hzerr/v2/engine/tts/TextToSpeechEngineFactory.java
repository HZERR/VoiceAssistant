package ru.hzerr.v2.engine.tts;

import ru.hzerr.v2.engine.tts.impl.YandexTextToSpeechEngine;

public class TextToSpeechEngineFactory {

    private final ITextToSpeechEngine yandexEngine;

    public TextToSpeechEngineFactory(YandexTextToSpeechEngine yandexEngine) {
        this.yandexEngine = yandexEngine;
    }

    public ITextToSpeechEngine getEngine(TextToSpeechConfiguration textToSpeechConfiguration) {
        return switch (textToSpeechConfiguration.getType()) {
            case YANDEX -> yandexEngine;
        };
    }
}
