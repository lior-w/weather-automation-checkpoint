package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CityFetcher {
    private final String url;

    public CityFetcher(String url) {
        this.url = url;
    }

    public List<City> getCities() {
        List<City> cities = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());

                // Parse the "cities" array
                JsonNode citiesArray = root.get("cities");
                if (citiesArray != null) {
                    for (JsonNode node : citiesArray) {
                        City city = new City();
                        city.setName(node.get("name").asText());
                        city.setRank(node.get("rank").asInt());
                        cities.add(city);
                    }
                } else {
                    System.out.println("No cities data found.");
                }
            } else {
                System.out.println("Failed to fetch cities: HTTP Status " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        return cities;
    }
}