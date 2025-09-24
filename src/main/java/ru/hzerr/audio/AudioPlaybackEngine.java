package ru.hzerr.audio;

import jakarta.annotation.PreDestroy;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.utils.DaemonThreadFactory;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AudioPlaybackEngine implements IAudioPlaybackEngine {

    private static final String ENDPOINT = "https://tts.api.cloud.yandex.net/tts/v3/utteranceSynthesis";
    private static final String ENDPOINT_V1 = "tts.api.cloud.yandex.net/speech/v1/tts:synthesize";
    private static final String EMOTION = "neutral"; // good, evil, neutral
    private static final String VOICE = "madi_ru";
    private static final Logger log = LoggerFactory.getLogger(AudioPlaybackEngine.class);
    private final ReadOnlyApplicationConfiguration readOnlyApplicationConfiguration;
    private final ExecutorService playbackQueue = Executors.newSingleThreadExecutor(new DaemonThreadFactory("Audio Playback Thread"));

    @Autowired
    public AudioPlaybackEngine(ReadOnlyApplicationConfiguration readOnlyApplicationConfiguration) {
        this.readOnlyApplicationConfiguration = readOnlyApplicationConfiguration;
    }

    @PreDestroy
    private void destroy() {
        playbackQueue.close();
        log.debug("✅ Модуль проигрывания звука успешно завершил работу");
    }

    @Override
    public void play(String text) {
        long startTime = System.currentTimeMillis();

        log.debug("🔊 Воспроизведение ответа началось...");
        playChunk(text);
        log.debug("✅ Воспроизведение ответа завершено за {}ms", System.currentTimeMillis() - startTime);
    }

    private void playChunk(String text) {
        if (text.isEmpty()) return;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize");

            // Устанавливаем заголовки
            post.setHeader("Authorization", "Api-Key " + readOnlyApplicationConfiguration.getYandexSecretKey());
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // Подготавливаем параметры
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("ssml", text.replace("Деваров", "[[d]]эв+аров").replaceAll("^```\\s*|\\s*```$", "")));
            params.add(new BasicNameValuePair("lang", "ru-RU"));
            params.add(new BasicNameValuePair("voice", VOICE));
//            params.add(new BasicNameValuePair("emotion", EMOTION));
            params.add(new BasicNameValuePair("speed", "1.0"));
            params.add(new BasicNameValuePair("format", "lpcm"));

            post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            byte[] audioData = httpClient.execute(post, response -> {
                if (response.getCode() == 200) {
                    return EntityUtils.toByteArray(response.getEntity());
                }

                String responseBody = EntityUtils.toString(response.getEntity());
                log.error("TTS Error: {}", responseBody);
                throw new IllegalStateException(responseBody);
            });

            // Отправляем в очередь на воспроизведение
            playbackQueue.submit(() -> {
                try {
                    play1(audioData);
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                    log.error("Ошибка при воспроизведении аудио", e);
                    throw new IllegalStateException(e);
                }
            });

        } catch (Exception e) {
            log.error("Ошибка в playChunk", e);
            throw new IllegalStateException(e);
        }
    }



    private void play0(byte[] bytes) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))) {
            AudioFormat format = audioStream.getFormat();

            // Ищем нужный output mixer
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            Mixer selectedMixer = null;

            for (Mixer.Info info : mixers) {
                if (info.getName().toLowerCase().contains("sound blasterx g6") &&
                        info.getDescription().toLowerCase().contains("playback")) {
                    selectedMixer = AudioSystem.getMixer(info);
                    System.out.println("🎧 Используем устройство: " + info.getName());
                    break;
                }
            }

            if (selectedMixer == null) {
                System.out.println("❌ Подходящее устройство вывода не найдено.");
                return;
            }

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!selectedMixer.isLineSupported(info)) {
                System.out.println("❌ Выбранное устройство не поддерживает формат: " + format);
                return;
            }

            try (SourceDataLine sourceLine = (SourceDataLine) selectedMixer.getLine(info)) {
                sourceLine.open(format);
                sourceLine.start();

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
                    sourceLine.write(buffer, 0, bytesRead);
                }

                // Завершаем проигрывание
                sourceLine.drain();
                sourceLine.stop();
                sourceLine.close();
                System.out.println("✅ Воспроизведение завершено.");
            }
        }
    }

    private void play1(byte[] bytes) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioFormat format = new AudioFormat(
                48000, // sampleRateHertz — указываешь тот же, что и в запросе
                16,    // sample size in bits
                1,     // mono (Yandex возвращает 1 канал)
                true,  // signed
                false  // little endian
        );

        try (SourceDataLine sourceLine = AudioSystem.getSourceDataLine(format)) {
            sourceLine.open(format);
            sourceLine.start();

            int bufferSize = 4096;
            int offset = 0;

            while (offset < bytes.length) {
                int toWrite = Math.min(bufferSize, bytes.length - offset);
                sourceLine.write(bytes, offset, toWrite);
                offset += toWrite;
            }

            sourceLine.drain();
            sourceLine.stop();
        }
//
//        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))) {
//            AudioFormat format = audioStream.getFormat();
//
//            try (SourceDataLine sourceLine = AudioSystem.getSourceDataLine(format)) {
//                sourceLine.open(format);
//                sourceLine.start();
//
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//
//                while ((bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
//                    sourceLine.write(buffer, 0, bytesRead);
//                }
//
//                sourceLine.drain();
//                sourceLine.stop();
//                sourceLine.close();
//
//            }
//        }
    }
}
