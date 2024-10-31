package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CoordinatesFetcher {
    private static final String API_KEY = "5abc828db62c2d79d0fd9f96e98059c6";
    private final String baseUrl;

    public CoordinatesFetcher(String url) {
        this.baseUrl = url;
    }

    public Coordinates getCoordinates(String cityName) {
        HttpClient client = HttpClient.newHttpClient();

        // URL-encode the city name
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&limit=1&appid=%s", baseUrl, encodedCityName, API_KEY);

        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
        } catch (IllegalArgumentException e) {
            System.err.println("Error creating URI: " + e.getMessage());
            return null;
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());

                if (root.isArray() && root.size() > 0) {
                    JsonNode cityNode = root.get(0);
                    double lat = cityNode.get("lat").asDouble();
                    double lon = cityNode.get("lon").asDouble();
                    return new Coordinates(lat, lon);
                } else {
                    System.out.println("No coordinates data found for the city.");
                }
            } else if (response.statusCode() == 401) {
                System.out.println("Unauthorized: Please check your API key.");
            } else {
                System.out.println("Failed to fetch coordinates: HTTP Status " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred during the HTTP request: " + e.getMessage());
        }

        return null;
    }
}

