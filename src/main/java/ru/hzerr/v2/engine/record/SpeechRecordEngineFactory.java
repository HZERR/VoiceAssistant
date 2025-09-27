package ru.hzerr.v2.engine.record;

import ru.hzerr.v2.engine.record.impl.SpeechRecordEngine;

public class SpeechRecordEngineFactory {

    private final ISpeechRecordEngine speechRecordEngine;

    public SpeechRecordEngineFactory(SpeechRecordEngine speechRecordEngine) {
        this.speechRecordEngine = speechRecordEngine;
    }

    public ISpeechRecordEngine getEngine(SpeechRecordConfiguration recordConfiguration) {
        return speechRecordEngine;
    }
}
