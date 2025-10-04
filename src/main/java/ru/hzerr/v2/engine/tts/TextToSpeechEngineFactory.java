package ru.hzerr.v2.engine.tts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.v2.engine.tts.impl.YandexTextToSpeechEngine;

@Component
public class TextToSpeechEngineFactory {

    private final ITextToSpeechEngine yandexEngine;

    @Autowired
    public TextToSpeechEngineFactory(YandexTextToSpeechEngine yandexEngine) {
        this.yandexEngine = yandexEngine;
    }

    public ITextToSpeechEngine getEngine(TextToSpeechConfiguration textToSpeechConfiguration) {
        return switch (textToSpeechConfiguration.getType()) {
            case YANDEX -> yandexEngine;
        };
    }
}
