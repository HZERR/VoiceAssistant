package ru.hzerr.v2.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.utils.JsonUtils;
import ru.hzerr.v2.command.CommandProcessorFactory;
import ru.hzerr.v2.engine.chatbot.*;
import ru.hzerr.v2.engine.play.AudioPlaybackConfiguration;
import ru.hzerr.v2.engine.play.AudioPlaybackEngineFactory;
import ru.hzerr.v2.engine.play.IAudioPlaybackEngine;
import ru.hzerr.v2.engine.record.ISpeechRecordEngine;
import ru.hzerr.v2.engine.record.SpeechRecordConfiguration;
import ru.hzerr.v2.engine.record.SpeechRecordEngineFactory;
import ru.hzerr.v2.engine.stt.ISpeechToTextEngine;
import ru.hzerr.v2.engine.stt.SpeechToTextConfiguration;
import ru.hzerr.v2.engine.stt.SpeechToTextEngineFactory;
import ru.hzerr.v2.engine.stt.SpeechToTextEngineType;
import ru.hzerr.v2.engine.tts.ITextToSpeechEngine;
import ru.hzerr.v2.engine.tts.TextToSpeechConfiguration;
import ru.hzerr.v2.engine.tts.TextToSpeechEngineFactory;
import ru.hzerr.v2.engine.tts.TextToSpeechEngineType;
import ru.hzerr.v2.exception.ProcessingException;
import ru.hzerr.v2.format.v1.ChatBotInstruction;

@Component
public class KeyboardListener implements NativeKeyListener {

    private static final Logger log = LoggerFactory.getLogger(KeyboardListener.class);
    private final ISpeechRecordEngine speechRecordEngine;
    private final ISpeechToTextEngine speechToTextEngine;
    private final IChatBotEngine<String> chatBotEngine;
    private final ITextToSpeechEngine textToSpeechEngine;
    private final IAudioPlaybackEngine audioPlaybackEngine;
    private final CommandProcessorFactory commandProcessorFactory;

    @Autowired
    @SuppressWarnings("unchecked")
    public KeyboardListener(SpeechRecordEngineFactory speechRecordEngineFactory,
                            SpeechToTextEngineFactory speechToTextEngineFactory,
                            ChatBotEngineFactory chatBotEngineFactory,
                            TextToSpeechEngineFactory textToSpeechEngineFactory,
                            AudioPlaybackEngineFactory audioPlaybackEngineFactory,
                            CommandProcessorFactory commandProcessorFactory) {

        SpeechRecordConfiguration speechRecordConfiguration = SpeechRecordConfiguration.builder().build();
        this.speechRecordEngine = speechRecordEngineFactory.getEngine(speechRecordConfiguration);
        SpeechToTextConfiguration speechToTextConfiguration = SpeechToTextConfiguration.builder().type(SpeechToTextEngineType.WHISPER).build();
        this.speechToTextEngine = speechToTextEngineFactory.getEngine(speechToTextConfiguration);
        ChatBotConfiguration chatBotConfiguration = ChatBotConfiguration.builder().type(ChatBotType.DEEPSEEK).build();
        this.chatBotEngine = chatBotEngineFactory.getEngine(chatBotConfiguration);
        TextToSpeechConfiguration textToSpeechConfiguration = TextToSpeechConfiguration.builder().type(TextToSpeechEngineType.YANDEX).build();
        this.textToSpeechEngine = textToSpeechEngineFactory.getEngine(textToSpeechConfiguration);
        AudioPlaybackConfiguration audioPlaybackConfiguration = AudioPlaybackConfiguration.builder().build();
        this.audioPlaybackEngine = audioPlaybackEngineFactory.getEngine(audioPlaybackConfiguration);
        this.commandProcessorFactory = commandProcessorFactory;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        try {
            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F7) {
                if (speechRecordEngine.nonActive()) {
                    speechRecordEngine.stop();

                    byte[] audio = speechRecordEngine.getAudio();
                    if (audio.length > 0) {
                        String text = speechToTextEngine.recognize(audio);
                        if (StringUtils.isNotEmpty(text)) {
                            ChatBotProcessingConfiguration chatBotProcessingConfiguration = new ChatBotProcessingConfiguration();
                            chatBotProcessingConfiguration.setRole(ChatBotRole.USER);
                            chatBotProcessingConfiguration.setMessage(text);
                            ChatBotInstruction chatBotInstruction = JsonUtils.read(chatBotEngine.process(chatBotProcessingConfiguration), ChatBotInstruction.class);
                            processChatBotInstruction(chatBotInstruction, 0);
                        } else
                            log.debug("🟡 Команда пуста");
                    } else
                        log.debug("🟡 Аудио не записано");
                } else
                    speechRecordEngine.start();
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

    private void processChatBotInstruction(ChatBotInstruction chatBotInstruction, int depth) throws ProcessingException, JsonProcessingException {
        if (depth > 2) throw new ProcessingException("🔴 Было предпринято слишком много системных вызовов");

        if (chatBotInstruction.hasSpeech())
            audioPlaybackEngine.play(textToSpeechEngine.synthesize(chatBotInstruction.getSpeak()));

        if (chatBotInstruction.hasActions()) {
            ChatBotProcessingConfiguration chatBotProcessingConfiguration = new ChatBotProcessingConfiguration();
            chatBotProcessingConfiguration.setRole(ChatBotRole.TOOLS);
            chatBotProcessingConfiguration.setMessage(commandProcessorFactory.process(chatBotInstruction.getCommands()));
            ChatBotInstruction newChatBotInstruction = JsonUtils.read(chatBotEngine.process(chatBotProcessingConfiguration), ChatBotInstruction.class);
            processChatBotInstruction(newChatBotInstruction, depth + 1);
        }
    }
}
