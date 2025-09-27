package ru.hzerr.v2.engine.stt.impl;

import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.recognizer.WhisperAudioRecognizer;
import ru.hzerr.utils.JsonUtils;
import ru.hzerr.v2.engine.stt.BaseSpeechToTextEngine;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class WhisperSpeechToTextEngine extends BaseSpeechToTextEngine {

    private static final Logger log = LoggerFactory.getLogger(WhisperSpeechToTextEngine.class);
    private static final String LOCAL_SERVER_URL = "http://localhost:8080";
    private static final String LOCAL_HEALTH_URL = "%s/health".formatted(LOCAL_SERVER_URL);
    private static final String LOCAL_INFERENCE_URL = "%s/inference".formatted(LOCAL_SERVER_URL);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Autowired
    public WhisperSpeechToTextEngine(ReadOnlyApplicationConfiguration applicationConfiguration) {
    }

    @Override
    protected String getModuleName() {
        return "Whisper";
    }

    @Override
    protected void onInitialize() throws Exception {
        log.debug("📦 Проверка подключения к серверу...");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOCAL_HEALTH_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.debug("✅ Сервер доступен");
            } else
                throw new IllegalStateException("❌ Сервер не доступен по адресу: %s. Статус: %s".formatted(LOCAL_SERVER_URL, response.statusCode()));
        } catch (Exception e) {
            throw new IllegalStateException("❌ Сервер не доступен по адресу: %s".formatted(LOCAL_SERVER_URL), e);
        }
    }

    @Override
    protected String onRecognize(byte[] input) throws Exception {
        byte[] wavAudioData = convertPcmToWav(input, 16000, 16, 1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOCAL_INFERENCE_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "audio/wav")
                .POST(HttpRequest.BodyPublishers.ofByteArray(wavAudioData))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.read(response.body()).path("text").asText();
        } else
            throw new IllegalStateException("❌ Сервер не доступен по адресу: %s. Статус: %s".formatted(LOCAL_INFERENCE_URL, response.statusCode()));
    }

    @Override
    protected void onDestroy() throws Exception {
        httpClient.close();
    }

    private static byte[] convertPcmToWav(byte[] pcmData, int sampleRate, int bitsPerSample, int channels) throws IOException {
        AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, true, false);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize())) {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
                return out.toByteArray();
            }
        }
    }
}
