package ru.hzerr.processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import ru.hzerr.processor.AssistantCommand;
import ru.hzerr.processor.IProcessor;
import ru.hzerr.utils.JsonUtils;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class TranslateProcessor implements IProcessor {

    protected abstract String getSourceLang();
    protected abstract String getTargetLang();

    @Override
    public String process(AssistantCommand command) {
        String textToTranslate;
        try {
            textToTranslate = (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return "[ОШИБКА] Не удалось получить текст из буфера обмена: " + e.getMessage();
        }

        if (textToTranslate == null || textToTranslate.isBlank()) {
            return "[ОШИБКА] Буфер обмена пуст или содержит не текст.";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            String requestBody = String.format(
                    "{\"q\":\"%s\",\"source\":\"%s\",\"target\":\"%s\",\"format\":\"text\"}",
                    textToTranslate.replace("\"", "\\\""), getSourceLang(), getTargetLang());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://libretranslate.com/translate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = JsonUtils.read(response.body());
                String translatedText = root.get("translatedText").asText();
                return "[ПЕРЕВОД]\n" + translatedText;
            } else
                return "[ОШИБКА] Ошибка API: " + response.statusCode() + " " + response.body();
        } catch (Exception e) {
            return "[ОШИБКА] Ошибка при вызове переводчика: " + e.getMessage();
        }
    }
}

