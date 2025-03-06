package com.example.demo.service;
import com.example.demo.config.OpenAIConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GPTService {

    private final OpenAIConfig openAIConfig;
    private final Gson gson;

    public GPTService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
        this.gson = new Gson();
    }

    /**
     * OpenAI ChatCompletion (예시)
     */
    public String generateCompletion(String systemInstruction, String userPrompt) {
        // 1) 메시지 배열
        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemInstruction);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        messages.add(userMsg);

        // 2) Request Body
        JsonObject requestBody = new JsonObject();
        requestBody.add("messages", messages);
        requestBody.addProperty("model", openAIConfig.getModel());
        requestBody.addProperty("max_tokens", 1024);
        requestBody.addProperty("temperature", 0.7);

        // 3) OkHttp 요청
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)    // 연결(Handshake) 타임아웃
                .writeTimeout(60, TimeUnit.SECONDS)      // 요청 Body 전송 타임아웃
                .readTimeout(120, TimeUnit.SECONDS)      // 응답 Body 수신 타임아웃
                .build();;

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(openAIConfig.getApiUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + openAIConfig.getApiKey())
                .post(body)
                .build();

        // 4) 응답
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI API 호출 실패: " + response);
            }
            String responseStr = response.body().string();

            // 5) JSON 파싱
            JsonObject responseJson = gson.fromJson(responseStr, JsonObject.class);
            JsonArray choices = responseJson.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject msgObj = choice.getAsJsonObject("message");
                return msgObj.get("content").getAsString().trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
