package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class App {

    private static final Random RANDOM = new Random();  // Random generator instance for varying sleep intervals
    private static final int MAX_RETRIES = 3;           // Max number of retries per request
    private static final int RETRY_DELAY = 2000;        // Delay in milliseconds between retries (2 seconds)

    public static void main(String[] args) {
        // URLs for the APIs to fetch city data, coordinates, and weather information
        String citiesUrl = "http://weather-automation-checkpoint-task.westeurope.cloudapp.azure.com:3000/cities";
        String coordinateBasesUrl = "http://api.openweathermap.org/geo/1.0/direct";
        String weatherBaseUrl = "https://api.openweathermap.org/data/2.5/weather";

        // Initialize fetcher instances for city data, coordinates, and weather data
        CityFetcher cityFetcher = new CityFetcher(citiesUrl);
        CoordinatesFetcher coordinatesFetcher = new CoordinatesFetcher(coordinateBasesUrl);
        WeatherFetcher weatherFetcher = new WeatherFetcher(weatherBaseUrl);

        // Retrieve the list of cities from the city fetcher
        List<City> cities = cityFetcher.getCities();
        List<Weather> weathers = new ArrayList<>();

        // Initialize cached thread pool and completion service for concurrent task handling
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletionService<Weather> completionService = new ExecutorCompletionService<>(executorService);

        // Submit each city's weather data retrieval as a task to the executor
        for (City c : cities) {
            completionService.submit(() -> {
                // Random sleep (200 - 1000 ms) to avoid overwhelming the API with requests
                int sleepTime = 200 + RANDOM.nextInt(800);
                try {
                    System.out.println("Sleeping for " + sleepTime + " ms to rate limit.");
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.err.println("Sleep interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }

                // Fetch coordinates with retry logic
                Coordinates coordinates = null;
                for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                    coordinates = coordinatesFetcher.getCoordinates(c.getName());
                    if (coordinates != null) break;  // Success, exit retry loop
                    if (attempt < MAX_RETRIES) {
                        System.out.println("Rate limit hit, retrying in " + RETRY_DELAY + " ms...");
                        try {
                            Thread.sleep(RETRY_DELAY);
                        } catch (InterruptedException e) {
                            System.err.println("Sleep interrupted: " + e.getMessage());
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                // If coordinates were retrieved, fetch the temperature with retry logic
                if (coordinates != null) {
                    Double temp = null;
                    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                        temp = weatherFetcher.getTemperature(coordinates.getLatitude(), coordinates.getLongitude());
                        if (temp != null) break;  // Success, exit retry loop
                        if (attempt < MAX_RETRIES) {
                            System.out.println("Rate limit hit, retrying in " + RETRY_DELAY + " ms...");
                            try {
                                Thread.sleep(RETRY_DELAY);
                            } catch (InterruptedException e) {
                                System.err.println("Sleep interrupted: " + e.getMessage());
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                    // Return weather data if temperature retrieval was successful
                    if (temp != null) {
                        return new Weather(c, temp);
                    }
                }
                return null; // Return null if weather data couldn't be fetched
            });
        }

        // Collect all completed weather tasks and store results in the weathers list
        for (int i = 0; i < cities.size(); i++) {
            try {
                Future<Weather> future = completionService.take(); // Retrieves next completed task
                Weather weather = future.get();
                if (weather != null) {
                    weathers.add(weather);
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error in fetching weather data: " + e.getMessage());
            }
        }

        // Shut down the executor service after completing all tasks
        executorService.shutdown();

        System.out.println("\n\nCities Temperature:\n");

        // Output all successfully fetched weather data to the console
        for (Weather w : weathers) {
            System.out.println(w);
        }
    }
}
