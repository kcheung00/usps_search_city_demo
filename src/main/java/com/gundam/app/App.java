package com.gundam.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;
import org.json.JSONObject;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    // API Configuration
    private static final String TOKEN_URL = "https://apis.usps.com/oauth2/v3/token";
    private static final String ADDRESS_URL = "https://apis.usps.com/addresses/v3/city-state?ZIPCode=94555";
    private static final String CLIENT_ID = "yWvrXoe1w99hF1H9jGkPvTtzNkLDtIxFdFmvW8VX9en3xy12"; // Replace with your actual client_id
    private static final String CLIENT_SECRET = "cASRb9tuGdWT5kAXKHbwWGnof9oSG8o7zCie4a1RO8K4TfrWdJS51qnDpMinsSnO"; // Replace with your actual client_secret

    public static void main(String[] args) {
        try {
        	
        	System.setProperty("http.proxyHost", "proxy.usps.gov");
        	System.setProperty("http.proxyPort", "8080");
        	System.setProperty("https.proxyHost", "proxy.usps.gov");
        	System.setProperty("https.proxyPort", "8080");
        	System.setProperty("https.protocols", "TLSv1.2");
        	
            // Initialize HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Step 1: Get OAuth Token
            String accessToken = getAccessToken(client);
            if (accessToken == null) {
                LOGGER.severe("Failed to retrieve access token. Exiting.");
                return;
            }
            LOGGER.info("Access Token retrieved successfully.");
            LOGGER.info(accessToken);

            // Step 2: Validate Address
            validateAddress(client, accessToken);
        } catch (Exception e) {
            LOGGER.severe("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getAccessToken(HttpClient client) throws Exception {
        // Build JSON body for token request
        JSONObject requestBody = new JSONObject();
        requestBody.put("client_id", CLIENT_ID);
        requestBody.put("client_secret", CLIENT_SECRET);
        requestBody.put("grant_type", "client_credentials");

        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response status
        if (response.statusCode() != 200) {
            LOGGER.severe("Token request failed with status: " + response.statusCode() + ", response: " + response.body());
            return null;
        }

        // Parse access token from response
        JSONObject jsonResponse = new JSONObject(response.body());
        String accessToken = jsonResponse.optString("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            LOGGER.severe("Access token not found in response: " + response.body());
            return null;
        }
        return accessToken;
    }

    private static void validateAddress(HttpClient client, String accessToken) throws Exception {
    	
        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ADDRESS_URL))
                .header("accept", "application/json")
                .header("x-user-id", "")
                .header("authorization", "Bearer " + accessToken)
                .GET()
                .build();

        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response status
        if (response.statusCode() != 200) {
            LOGGER.severe("Address request failed with status: " + response.statusCode() + ", response: " + response.body());
            return;
        }

        // Log the response
        LOGGER.info("Address Validation Response: " + response.body());
    }
}