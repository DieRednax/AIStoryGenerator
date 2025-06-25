package com.redfox.ai_story_generator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AIClient {
    private static final HttpClient client = HttpClient.newHttpClient();

    public enum AIModel {
        CHAT_GPT("CHAT_GPT"),
        CLAUDE("CLAUDE"),
        GEMINI("GEMINI");

        public final String aiModel;

        AIModel(String aiModel) {
            this.aiModel = aiModel;
        }

        @Override
        public String toString() {
            return this.aiModel;
        }

        public static AIModel fromAIModel(String aiModel) {
            for (AIModel v : values()) {
                if (v.aiModel.equalsIgnoreCase(aiModel)) {
                    return v;
                }
            }
            throw new IllegalArgumentException("Unknown ai model: " + aiModel);
        }
    }
    public enum AIVersion {
        /**
         * GPT
         */
        GPT_o4_MINI("o4-mini"),
        GPT_o3("o3"),
        GPT_4_1("gpt-4.1"),
        GPT_4o("gpt-4o"),
        GPT_CHAT_4o("chatgpt-4o-latest"),
        GPT_3_5_TURBO("gpt-3.5-turbo"),
        GPT_4o_MINI("gpt-4o-mini"),
        GPT_4_1_MINI("gpt-4.1-mini"),
        /**
         * CLAUDE
         */
        CLAUDE_OPUS_4("claude-opus-4-20250514"),
        CLAUDE_SONNET_4("claude-sonnet-4-20250514"),
        CLAUDE_SONNET_3_7("claude-3.7-sonnet-20250219"),
        CLAUDE_HAIKU_3_5("claude-3.5-haiku-20241022"),
        CLAUDE_OPUS_3("claude-3-opus-20240229"),
        /**
         * GEMINI
         */
        GEMINI_2_5_PRO("gemini-2.5-pro"),
        GEMINI_2_5_FLASH("gemini-2.5-flash"),
        GEMINI_2_5_FLASH_LITE_PREV("gemini-2.5-flash-lite-preview-06-17"),
        GEMINI_2_0_FLASH("gemini-2.0-flash"),
        GEMINI_2_0_FLASH_LITE("gemini-2.0-flash-lite"),
        GEMINI_1_5_FLASH("gemini-1.5-flash"),
        GEMINI_1_5_PRO("gemini-1.5-pro");

        public final String modelName;

        AIVersion(String modelName) {
            this.modelName = modelName;
        }

        @Override
        public String toString() {
            return modelName;
        }

        public static AIVersion fromModelName(String modelName) {
            for (AIVersion v : values()) {
                if (v.modelName.equalsIgnoreCase(modelName)) {
                    return v;
                }
            }
            throw new IllegalArgumentException("Unknown ai version: " + modelName);
        }
    }

    public static String askAI(String prompt, String apiKey, AIModel aiModel, AIVersion aiVersion) throws IOException, InterruptedException {
        String endpoint = "";
        String jsonBody = "";

        HttpRequest request;

        switch (aiModel) {
            case CHAT_GPT -> {
                endpoint = "https://api.openai.com/v1/chat/completions";
                jsonBody = """
                        {
                            "model": "%s",
                            "messages": [
                                {"role": "user", "content": "%s"}
                            ]
                        }
                        """.formatted(aiVersion.toString(), prompt);
                request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(120))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
            }
            case CLAUDE -> {
                endpoint = "https://api.anthropic.com/v1/messages";
                jsonBody = """
                        {
                            "model": "%s",
                            "max_tokens": 512,
                            "messages": [
                                {"role": "user", "content": "%s"}
                            ]
                        }
                        """.formatted(aiVersion.toString(), prompt);

                request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(120))
                        .header("Content-Type", "application/json")
                        .header("x-api-key", apiKey)
                        .header("anthropic-version", "2023-06-01")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
            }
            case GEMINI -> {
                endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                        + aiVersion.toString()
                        + ":generateContent?key="
                        + apiKey;

                jsonBody = """
                        {
                            "contents": [{
                                "parts": [{"text": "%s"}]
                            }]
                        }
                        """.formatted(prompt);

                request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofSeconds(120))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
            }
            default -> throw new IllegalArgumentException("unsupported ai model: " + aiModel);
        }

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error: " + response.statusCode() + "\n" + new String(response.body(), StandardCharsets.UTF_8));
        }

        return new String(response.body(), StandardCharsets.UTF_8);
    }

    public static String extractChatGPTContent(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        return jsonObject
                .getAsJsonArray("choices")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }
}
