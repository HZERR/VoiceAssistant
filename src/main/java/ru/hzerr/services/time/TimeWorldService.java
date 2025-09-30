package ru.hzerr.services.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

//@Component
public class TimeWorldService {

    private static final Logger log = LoggerFactory.getLogger(TimeWorldService.class);

    // Используй HTTPS gateway
    private static final String BASE_URL = "https://world-time-api3.p.rapidapi.com";
    private static final String TIMEZONE_LIST_PATH = "/timezone";
    private final ReadOnlyApplicationConfiguration applicationConfiguration;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public TimeWorldService(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.applicationConfiguration = applicationConfiguration;
    }

    public String[] getTimezones() throws Exception {
        String url = BASE_URL + TIMEZONE_LIST_PATH;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), String[].class);
        } else {
            throw new IOException("Failed to list timezones: HTTP " + response.statusCode());
        }
    }

    public WorldTimeResponse getTimeByTimezone(String timezone) throws InterruptedException {
        String path = "/timezone/" + timezone;
        String url = BASE_URL + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-rapidapi-key", applicationConfiguration.getTimeWorldApiKey())
                .header("x-rapidapi-host", "world-time-api3.p.rapidapi.com")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        IOException lastIoEx = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                log.debug("⚙️ Запрос времени у timeapi.world: попытка {} для таймзоны {}", attempt, timezone);
                long start = System.currentTimeMillis();

                HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                long duration = System.currentTimeMillis() - start;
                log.debug("✅ Ответ получен от timeapi.world за {}ms", duration);

                if (resp.statusCode() == 200) {
                    WorldTimeResponse wtr = objectMapper.readValue(resp.body(), WorldTimeResponse.class);
                    return wtr;
                } else {
                    log.warn("⚠️ timeapi.world вернул статус {} для таймзоны {}", resp.statusCode(), timezone);
                }
            } catch (IOException e) {
                lastIoEx = e;
                log.warn("⚠️ Ошибка при запросе timeapi.world (таймзона {}), попытка {}: {}", timezone, attempt, e.getMessage());
                // подожди перед следующим
                Thread.sleep(500 * attempt);
            }
        }

        // Если все попытки неуспешны — бросить исключение
        throw new RuntimeException("Не удалось получить время по таймзоне " + timezone + (lastIoEx != null ? ": " + lastIoEx.getMessage() : ""));
    }
}

