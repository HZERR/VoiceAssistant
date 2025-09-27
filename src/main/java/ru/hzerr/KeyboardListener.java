package ru.hzerr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.audio.IAudioPlaybackEngine;
import ru.hzerr.chat.AssistantProcessingOptions;
import ru.hzerr.chat.AssistantResponse;
import ru.hzerr.chat.IAssistant;
import ru.hzerr.processor.CommandProcessorFactory;
import ru.hzerr.recognizer.IRecognizer;
import ru.hzerr.recorder.IRecorder;
import ru.hzerr.utils.JsonUtils;

@Component
public class KeyboardListener implements NativeKeyListener {

    private static final Logger log = LoggerFactory.getLogger(KeyboardListener.class);
    private final IRecorder recorder;
    private final IRecognizer recognizer;
    private final IAssistant assistant;
    private final CommandProcessorFactory commandProcessorFactory;
    private final IAudioPlaybackEngine audioPlaybackEngine;

    @Autowired
    public KeyboardListener(IRecorder recorder, IRecognizer recognizer, CommandProcessorFactory commandProcessorFactory, IAssistant assistant, IAudioPlaybackEngine audioPlaybackEngine) {
        this.recognizer = recognizer;
        this.recorder = recorder;
        this.commandProcessorFactory = commandProcessorFactory;
        this.assistant = assistant;
        this.audioPlaybackEngine = audioPlaybackEngine;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        try {
            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F7) {
                if (recorder.isRecording()) {
                    recorder.stop();

                    byte[] audioByteArray = recorder.getAudioStream().toByteArray();
                    if (audioByteArray.length > 0) {
                        String input = recognizer.recognize(audioByteArray);
                        if (StringUtils.isNotEmpty(input)) {
                            log.debug("🤖 Начата обработка команды '{}' голосовым ассистентом", input);
                            processAssistant(input, new AssistantProcessingOptions("user"));
                        } else
                            log.debug("🟡 Команда пуста");
                    } else
                        log.debug("🟡 Аудио не записано");
                } else
                    recorder.start();
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

    private void processAssistant(String message, AssistantProcessingOptions assistantProcessingOptions) throws JsonProcessingException {
        AssistantResponse assistantResponse = JsonUtils.read(assistant.process(message, assistantProcessingOptions).replaceAll("^```\\s*|\\s*```$", ""), AssistantResponse.class);
        log.debug("📥 Краткий ответ ассистента: {}", assistantResponse.getSpeak());

        audioPlaybackEngine.play(assistantResponse.getSpeak());

        if (!assistantResponse.getCommands().isEmpty())
            processAssistant(commandProcessorFactory.process(assistantResponse.getCommands()), new AssistantProcessingOptions("system"));
    }
}
