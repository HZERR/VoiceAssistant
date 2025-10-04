package ru.hzerr.v2.engine.stt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.v2.engine.stt.impl.VoskSpeechToTextEngine;
import ru.hzerr.v2.engine.stt.impl.WhisperSpeechToTextEngine;

@Component
public class SpeechToTextEngineFactory {

    private final ISpeechToTextEngine whisperEngine;
    private final ISpeechToTextEngine voskEngine;

    @Autowired
    public SpeechToTextEngineFactory(WhisperSpeechToTextEngine whisperEngine,
                                     VoskSpeechToTextEngine voskEngine) {

        this.whisperEngine = whisperEngine;
        this.voskEngine = voskEngine;
    }

    public ISpeechToTextEngine getEngine(SpeechToTextConfiguration speechToTextConfiguration) {
        return switch (speechToTextConfiguration.getType()) {
            case WHISPER -> whisperEngine;
            case VOSK -> voskEngine;
        };
    }
}
