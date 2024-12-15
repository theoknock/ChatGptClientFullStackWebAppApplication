package com.example.chatgptclientfullstackwebappapplication.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/chatgpt")
public class ChatGptController {

    @PostMapping("/send")
    public String sendPrompt(@RequestBody String userPrompt) {
        // API Endpoint (OpenAI Chat Completions)
        String apiURL = "https://api.openai.com/v1/chat/completions";

        // Replace with your OpenAI API Key
        String apiKey = "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";

        // Align request body with the Swing app format
        String requestBody = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"You are an intelligent assistant.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}\n" +
                "  ],\n" +
                "  \"max_tokens\": 16384,\n" +
                "  \"stream\": true\n" +
                "}";

        try {
            // Create HTTP client and request matching Swing app
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))  // POST body
                    .build();

            // Send request and process streamed response
            HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            // Check if response status is 200 OK
            if (response.statusCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
                StringBuilder responseContent = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring("data: ".length()).trim();
                        if (data.equals("[DONE]")) {
                            // End of the streaming response
                            break;
                        }
                        String content = extractContent(data); // Extract "content" field
                        if (content != null) {
                            responseContent.append(content).append("\n");
                        }
                    }
                }

                return responseContent.toString();  // Final response, combined
            } else {
                // Handle non-200 responses
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
                StringBuilder errorContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorContent.append(line);
                }
                return "Error: API returned status code " + response.statusCode() + " - " + errorContent;
            }
        } catch (Exception e) {
            // Handle exceptions and errors
            return "Error: Exception occurred while sending request - " + e.getMessage();
        }
    }

    // Escape JSON as done in the Swing app
    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // Extract the "content" field from each data chunk, same as Swing app logic
    private static String extractContent(String jsonData) {
        int contentIndex = jsonData.indexOf("\"content\":");
        if (contentIndex == -1) {
            return null;
        }

        int startQuoteIndex = jsonData.indexOf("\"", contentIndex + "\"content\":".length());
        if (startQuoteIndex == -1) {
            return null;
        }

        int endQuoteIndex = jsonData.indexOf("\"", startQuoteIndex + 1);
        while (endQuoteIndex != -1 && jsonData.charAt(endQuoteIndex - 1) == '\\') {
            // Skip escaped quotes
            endQuoteIndex = jsonData.indexOf("\"", endQuoteIndex + 1);
        }
        if (endQuoteIndex == -1) {
            return null;
        }

        String content = jsonData.substring(startQuoteIndex + 1, endQuoteIndex);

        // Unescape characters
        return content.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}