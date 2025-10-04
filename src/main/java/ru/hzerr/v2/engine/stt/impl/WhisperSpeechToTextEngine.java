package ru.hzerr.v2.engine.stt.impl;

import org.apache.commons.lang3.RegExUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.utils.DaemonThreadFactory;
import ru.hzerr.utils.JsonUtils;
import ru.hzerr.v2.engine.stt.BaseSpeechToTextEngine;
import ru.hzerr.v2.exception.ProcessingException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class WhisperSpeechToTextEngine extends BaseSpeechToTextEngine {

    private static final Logger log = LoggerFactory.getLogger(WhisperSpeechToTextEngine.class);
    private static final String LOCAL_SERVER_URL = "http://localhost:10001";
    private static final String LOCAL_HEALTH_URL = "%s/health".formatted(LOCAL_SERVER_URL);
    private static final String LOCAL_INFERENCE_URL = "%s/inference".formatted(LOCAL_SERVER_URL);
    private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private static final CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
    private Process whisperProcess;
    private Thread whisperDebugThread;
    private Thread whisperErrorThread;

    private final ReadOnlyApplicationConfiguration applicationConfiguration;

    @Autowired
    public WhisperSpeechToTextEngine(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    protected String getModuleName() {
        return "Whisper";
    }

    @Override
    protected void onInitialize() throws Exception {
        Files.createDirectories(applicationConfiguration.getProgramDirectory());
        Files.createDirectories(applicationConfiguration.getWhisperDirectory());
        try (ZipInputStream zis = new ZipInputStream(Objects.requireNonNull(WhisperSpeechToTextEngine.class.getResourceAsStream(applicationConfiguration.getWhisperProgramRelativePath())))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                Path target = applicationConfiguration.getWhisperDirectory().resolve(RegExUtils.removeFirst(entry.getName(), "Release/"));
                if (Files.notExists(target)) {
                    Files.copy(zis, target);
                }
            }
        }

        Path target = applicationConfiguration.getWhisperDirectory().resolve(applicationConfiguration.getWhisperMediumQ8ModelName());
        if (Files.notExists(target)) {
            Files.copy(Objects.requireNonNull(WhisperSpeechToTextEngine.class.getResourceAsStream(applicationConfiguration.getWhisperMediumQ8ModelRelativePath())), target);
        }
        log.debug("✅ Файлы модуля распознавания речи успешно подготовлены");

        log.debug("🚀 Запуск локального сервера распознавания речи...");
        Path whisperServer = applicationConfiguration.getWhisperDirectory().resolve("whisper-server.exe");
        ProcessBuilder whisperProcessBuilder = new ProcessBuilder(
                whisperServer.toString(),
                "--model", applicationConfiguration.getWhisperMediumQ8ModelName(),
                "--port", "10001",
                "--language", "ru",
                "--threads", "8",
                "--flash-attn"
        );
        whisperProcessBuilder.directory(applicationConfiguration.getWhisperDirectory().toFile());
        whisperProcess = whisperProcessBuilder.start();
        Thread.sleep(3000);

        whisperDebugThread = DaemonThreadFactory.newFactory("Whisper Debug").newThread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(whisperProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug(line);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
        whisperDebugThread.start();
        whisperErrorThread = DaemonThreadFactory.newFactory("Whisper Error").newThread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(whisperProcess.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.error(line);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
        whisperErrorThread.start();
        log.debug("✅ Локальный сервер модуля распознавания речи успешно поднят");

        waitForServer();
    }

    @Override
    protected String onRecognize(byte[] input) throws Exception {
        byte[] wavAudioData = convertPcmToWav(input, 16000, 16, 1);

        HttpPost post = new HttpPost(LOCAL_INFERENCE_URL);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.STRICT)
                .addBinaryBody("file", new ByteArrayInputStream(wavAudioData), ContentType.create("audio/wav"), "audio.wav");

        post.setEntity(builder.build());

        try (CloseableHttpResponse response = closeableHttpClient.execute(post)) {
            int status = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            if (status == 200) {
                return JsonUtils.read(responseBody).path("text").asText();
            } else {
                throw new IllegalStateException("❌ Сервер не доступен по адресу: %s. Статус: %s".formatted(LOCAL_INFERENCE_URL, status));
            }
        } catch (Exception e) {
            log.error("❌ Ошибка при обращении к локальному серверу", e);
            throw new ProcessingException("Ошибка распознавания аудио", e);
        }
    }

    @Override
    protected void onDestroy() throws Exception {
        httpClient.close();

        if (whisperProcess != null && whisperProcess.isAlive()) {
            log.debug("⚙️ Завершаем работу сервера модуля распознавания речи...");
            whisperProcess.destroy();

            try {
                if (!whisperProcess.waitFor(Duration.ofSeconds(5))) {
                    whisperProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                whisperProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            log.debug("✅ Работа сервера модуля распознавания речи успешно завершена");
        }
    }

    public byte[] convertPcmToWav(byte[] pcmData, int sampleRate, int bitsPerSample, int channels) throws IOException {
        AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, true, false);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(pcmData), format, pcmData.length / format.getFrameSize())) {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
                return out.toByteArray();
            }
        }
    }

//    public static byte[] convertPcmToWav(byte[] pcmData, int sampleRate, int bitsPerSample, int channels) throws IOException {
//        int byteRate = sampleRate * channels * bitsPerSample / 8;
//        int dataSize = pcmData.length;
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        // RIFF header
//        out.write("RIFF".getBytes());
//        out.write(intToByteArray(36 + dataSize)); // ChunkSize
//        out.write("WAVE".getBytes());
//
//        // fmt subchunk
//        out.write("fmt ".getBytes());
//        out.write(intToByteArray(16)); // Subchunk1Size for PCM
//        out.write(shortToByteArray((short)1)); // AudioFormat PCM
//        out.write(shortToByteArray((short)channels));
//        out.write(intToByteArray(sampleRate));
//        out.write(intToByteArray(byteRate));
//        out.write(shortToByteArray((short)(channels * bitsPerSample / 8))); // BlockAlign
//        out.write(shortToByteArray((short)bitsPerSample));
//
//        // data subchunk
//        out.write("data".getBytes());
//        out.write(intToByteArray(dataSize));
//        out.write(pcmData);
//
//        return out.toByteArray();
//    }

    private static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value & 0xff),
                (byte)((value >> 8) & 0xff),
                (byte)((value >> 16) & 0xff),
                (byte)((value >> 24) & 0xff)
        };
    }

    private static byte[] shortToByteArray(short value) {
        return new byte[] {
                (byte)(value & 0xff),
                (byte)((value >> 8) & 0xff)
        };
    }

    public void waitForServer() {
        log.debug("📦 Ожидание запуска сервера...");

        final int maxAttempts = 10;
        final Duration delay = Duration.ofSeconds(3);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(LOCAL_HEALTH_URL))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    log.debug("✅ Сервер доступен (попытка {}/{})", attempt, maxAttempts);
                    return;
                } else {
                    log.debug("⚠️ Сервер отвечает с статусом {} (попытка {}/{})", response.statusCode(), attempt, maxAttempts);
                }
            } catch (Exception e) {
                log.debug("⏳ Сервер не доступен, попытка {}/{}: {}", attempt, maxAttempts, e.getMessage());
            }

            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("❌ Ожидание сервера прервано", ie);
                }
            }
        }

        throw new IllegalStateException("❌ Сервер не запустился после %d попыток".formatted(maxAttempts));
    }
}
