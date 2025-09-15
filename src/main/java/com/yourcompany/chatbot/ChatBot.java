package com.yourcompany.chatbot;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatBot {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String API_KEY = "AIzaSyApgTvN_cgFLjdzwoix7xAgO9YaVXKbfMA";

    private static final String COLOR_RESET = "\u001B[0m";
    private static final String COLOR_HEADER = "\u001B[95m"; // Magenta
    private static final String COLOR_BULLET = "\u001B[94m"; // Blue

    public String getResponse(String userInput) {
        try {
            String rawResponse = getGeminiResponse(userInput);
            return colorizeText(rawResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I encountered an error. " + e.getMessage();
        }
    }

    private String getGeminiResponse(String userInput) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(API_URL + "?key=" + API_KEY);

        httpPost.setHeader("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject contentObject = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject partObject = new JSONObject();

        partObject.put("text", userInput);
        partsArray.put(partObject);
        contentObject.put("parts", partsArray);
        contentObject.put("role", "user");
        contentsArray.put(contentObject);

        requestBody.put("contents", contentsArray);

        JSONArray safetySettings = new JSONArray();
        JSONObject safetySetting = new JSONObject();
        safetySetting.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        safetySetting.put("threshold", "BLOCK_ONLY_HIGH");
        safetySettings.put(safetySetting);
        requestBody.put("safetySettings", safetySettings);

        httpPost.setEntity(new StringEntity(requestBody.toString()));
        CloseableHttpResponse response = httpClient.execute(httpPost);

        try {
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);

            JSONObject jsonResponse = new JSONObject(responseString);

            if (jsonResponse.has("error")) {
                return "Gemini error: " + jsonResponse.getJSONObject("error").getString("message");
            }

            if (jsonResponse.has("candidates")) {
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    if (candidate.has("content")) {
                        JSONObject content = candidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            return parts.getJSONObject(0).getString("text");
                        }
                    }
                }
            }
            return "Unexpected response format from Gemini";
        } finally {
            response.close();
            httpClient.close();
        }
    }

    private String colorizeText(String text) {
        StringBuilder coloredText = new StringBuilder();

        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("**")) {
                int starIndex = line.indexOf("**");
                String cleaned = line.substring(starIndex + 2).trim();
                if (cleaned.endsWith("**")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 2).trim();
                }
                coloredText.append(COLOR_HEADER).append(cleaned).append(COLOR_RESET);
            } else if (trimmed.startsWith("*")) {
                int starIndex = line.indexOf("*");
                String cleaned = line.substring(starIndex + 1).trim();
                cleaned = colorBoldText(cleaned);
                coloredText.append(COLOR_BULLET).append("• ").append(cleaned).append(COLOR_RESET);
            } else {
                coloredText.append(processInlineBold(line));
            }
            coloredText.append("\n");
        }
        return coloredText.toString();
    }

    private String colorBoldText(String text) {
        StringBuilder result = new StringBuilder();

        int index = 0;
        while (index < text.length()) {
            int start = text.indexOf("**", index);
            if (start == -1) {
                result.append(text.substring(index));
                break;
            }
            int end = text.indexOf("**", start + 2);
            if (end == -1) {
                result.append(text.substring(index));
                break;
            }
            result.append(text.substring(index, start));
            String boldText = text.substring(start + 2, end);
            result.append(COLOR_HEADER).append(boldText).append(COLOR_RESET);
            index = end + 2;
        }
        return result.toString();
    }

    private String processInlineBold(String line) {
        return colorBoldText(line);
    }
}

