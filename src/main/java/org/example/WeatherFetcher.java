package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

public class WeatherFetcher {
    private static final String API_KEY = "5abc828db62c2d79d0fd9f96e98059c6";
    private final String baseUrl;

    public WeatherFetcher(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Double getTemperature(double lat, double lon) {
        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric", baseUrl, lat, lon, API_KEY);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());

                // Parse the temperature from JSON
                JsonNode mainNode = root.get("main");
                if (mainNode != null && mainNode.has("temp")) {
                    double temperature = mainNode.get("temp").asDouble();
                    return temperature;
                } else {
                    System.out.println("Temperature data not found.");
                }
            } else {
                System.out.println("Failed to fetch weather: HTTP Status " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        return null;
    }
}
