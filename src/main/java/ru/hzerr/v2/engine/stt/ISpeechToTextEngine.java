package ru.hzerr.v2.engine.stt;

import ru.hzerr.v2.exception.ProcessingException;

public interface ISpeechToTextEngine {

    String recognize(byte[] input) throws ProcessingException;
}
