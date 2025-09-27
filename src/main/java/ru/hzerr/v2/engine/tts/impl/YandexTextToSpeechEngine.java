package ru.hzerr.v2.engine.tts.impl;

import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.v2.engine.tts.BaseTextToSpeechEngine;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class YandexTextToSpeechEngine extends BaseTextToSpeechEngine {

    private static final String ENDPOINT_V3 = "https://tts.api.cloud.yandex.net/tts/v3/utteranceSynthesis";
    private static final String ENDPOINT_V1 = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize";
    private static final String EMOTION = "neutral"; // good, evil, neutral
    private static final String VOICE = "madi_ru";
    private final ReadOnlyApplicationConfiguration applicationConfiguration;

    @Autowired
    public YandexTextToSpeechEngine(ReadOnlyApplicationConfiguration applicationConfiguration) {
        super();

        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    protected String getModuleName() {
        return "Yandex";
    }

    @Override
    protected void onInitialize() throws Exception {

    }

    @Override
    protected byte[] onSynthesize(String input) throws Exception {
        StringBuilder formData = new StringBuilder();
        formData.append("ssml=").append(URLEncoder.encode(input.replace("Деваров", "[[d]]эв+аров").replaceAll("^```\\s*|\\s*```$", ""), StandardCharsets.UTF_8));
        formData.append("&lang=").append(URLEncoder.encode("ru-RU", StandardCharsets.UTF_8));
        formData.append("&voice=").append(URLEncoder.encode(VOICE, StandardCharsets.UTF_8));
// formData.append("&emotion=").append(URLEncoder.encode(EMOTION, StandardCharsets.UTF_8));
        formData.append("&speed=").append(URLEncoder.encode("1.0", StandardCharsets.UTF_8));
        formData.append("&format=").append(URLEncoder.encode("lpcm", StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT_V1))
                .header("Authorization", "Api-Key " + applicationConfiguration.getYandexSecretKey())
                .header("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                .POST(HttpRequest.BodyPublishers.ofString(formData.toString()))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            return response.body();
        }

        throw new IllegalStateException("❌ Сервер '%s' не обработал запрос. Статус: %s".formatted(ENDPOINT_V1, response.statusCode()));
    }

    @Override
    protected void onDestroy() throws Exception {
    }
}
