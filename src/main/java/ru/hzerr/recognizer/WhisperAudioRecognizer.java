package ru.hzerr.recognizer;

import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.utils.JsonUtils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class WhisperAudioRecognizer implements IRecognizer {

    private static final Logger log = LoggerFactory.getLogger(WhisperAudioRecognizer.class);
    private static final String LOCAL_SERVER_URL = "http://localhost:8080";
    private static final String LOCAL_HEALTH_URL = "%s/health".formatted(LOCAL_SERVER_URL);
    private final CloseableHttpClient httpClient;

    @Autowired
    public WhisperAudioRecognizer(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.httpClient = HttpClients.custom().build();
    }

    @PostConstruct
    private void init() {
        log.debug("📦 Проверка подключения к серверу...");

        try {
            try (ClassicHttpResponse response = httpClient.execute(new HttpGet(LOCAL_HEALTH_URL))) {
                int status = response.getCode();
                if (status == 200) {
                    log.debug("✅ Whisper сервер доступен");
                } else {
                    throw new IllegalStateException("Whisper Server вернул статус: " + status);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Whisper сервер не доступен по адресу: " + LOCAL_SERVER_URL, e);
        }
    }

    // whisper-server.exe --model ggml-small.bin --port 8080 --language ru --threads 8 --flash-attn
    // whisper-server.exe --model ggml-medium-q8_0.bin --port 8080 --language ru --threads 8 --flash-attn
    // whisper-server.exe --model ggml-medium-q8_0.bin --port 8080 --threads 4 --language ru --max-len 1 --flash-attn
    @Override
    public String recognize(byte[] audioData) {
        long startTime = System.currentTimeMillis();
        log.debug("🧠 Отправка {} байт аудио на распознавание...", audioData.length);

        HttpPost post = new HttpPost(LOCAL_SERVER_URL + "/inference");

        // Создаем multipart с полем "file", передаём байты из памяти
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        try {
            builder.addBinaryBody(
                    "file",
                    new ByteArrayInputStream(convertPcmToWav(audioData, 16000, 16, 1)),
                    ContentType.create("audio/wav"),
                    "audio.wav"
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        post.setEntity(builder.build());

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int status = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (status == 200) {
                log.debug("✅ Распознавание завершено за {}ms: {}", System.currentTimeMillis() - startTime, responseBody);
                return JsonUtils.read(responseBody).path("text").asText();
            } else {
                throw new IOException("HTTP ошибка: " + status + ", тело ответа: " + responseBody);
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при обращении к Whisper Server", e);
            throw new IllegalStateException("Ошибка распознавания аудио");
        }
    }

    public static byte[] convertPcmToWav(byte[] pcmData, int sampleRate, int bitsPerSample, int channels) throws IOException {
        AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, true, false);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize())) {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
                return out.toByteArray();
            }
        }
    }
}
