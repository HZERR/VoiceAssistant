package ru.hzerr.v2.engine.tts;

import ru.hzerr.v2.exception.ProcessingException;

public interface ITextToSpeechEngine {

    byte[] synthesize(String input) throws ProcessingException;
}
