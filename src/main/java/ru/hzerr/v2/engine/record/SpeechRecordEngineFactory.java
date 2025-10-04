package ru.hzerr.v2.engine.record;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.v2.engine.record.impl.SpeechRecordEngine;

@Component
public class SpeechRecordEngineFactory {

    private final ISpeechRecordEngine speechRecordEngine;

    @Autowired
    public SpeechRecordEngineFactory(SpeechRecordEngine speechRecordEngine) {
        this.speechRecordEngine = speechRecordEngine;
    }

    public ISpeechRecordEngine getEngine(SpeechRecordConfiguration recordConfiguration) {
        return speechRecordEngine;
    }
}
