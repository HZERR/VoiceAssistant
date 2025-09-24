package ru.hzerr.services.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class WorldTimeService {

    private static final String BASE_URL = "http://worldtimeapi.org/api";
    private static final String TIMEZONE_LIST_URL = "https://worldtimeapi.org/api/timezone";
    private final HttpClient httpClient;

    public WorldTimeService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String[] getTimezones() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TIMEZONE_LIST_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return new ObjectMapper().readValue(response.body(), String[].class);
    }

    public WorldTimeResponse getTimeByTimezone(String timezone) throws IOException, InterruptedException {
        String url = BASE_URL + "/timezone/" + timezone;
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new ObjectMapper().readValue(response.body(), WorldTimeResponse.class);
        } else {
            throw new RuntimeException("Failed to get time: HTTP " + response.statusCode());
        }
    }

    public static void main(String[] args) {
        WorldTimeService client = new WorldTimeService();
        try {
//            Arrays.stream(client.getTimezones()).forEach(System.out::println);
            WorldTimeResponse response = client.getTimeByTimezone("America/New_York"); // America/New_York
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
