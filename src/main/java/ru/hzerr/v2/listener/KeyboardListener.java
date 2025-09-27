package ru.hzerr.v2.listener;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.hzerr.v2.engine.stt.SpeechToTextEngineFactory;
import ru.hzerr.v2.engine.tts.TextToSpeechEngineFactory;

public class KeyboardListener implements NativeKeyListener {

    private static final Logger log = LoggerFactory.getLogger(KeyboardListener.class);
    private final SpeechToTextEngineFactory speechToTextEngineFactory;
    private final TextToSpeechEngineFactory textToSpeechEngineFactory;
    // e.t.c

    @Autowired
    public KeyboardListener(SpeechToTextEngineFactory speechToTextEngineFactory,
                            TextToSpeechEngineFactory textToSpeechEngineFactory) {

        this.speechToTextEngineFactory = speechToTextEngineFactory;
        this.textToSpeechEngineFactory = textToSpeechEngineFactory;
        // e.t.c
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        try {
            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F7) {
                // какая-то логика
            }

            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F8) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}
}
