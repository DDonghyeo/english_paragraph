package com.example.demo.service;

import com.example.demo.config.OpenAIConfig;
import com.example.demo.dto.GPTMessageDto;
import com.example.demo.dto.request.GPTThreadRunRequestDto;
import com.example.demo.dto.response.GPTResponseDto;
import com.example.demo.dto.response.GPTThreadMessagesResponseDto;
import com.example.demo.dto.response.ParagraphResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GPTService {

    private final OpenAIConfig openAIConfig;
    private final ObjectMapper objectMapper;

    private final String analysisAssistantId = "asst_7F8tSAM1IluIGFZERbw8i4FS"; // 문단 분석 Assistant
    private final String refreshAssistantId = "asst_m4sxaWC7YaAcUCjgXkLH8TnQ"; // 특정 섹션 재생성 Assistant

    // ✅ 1️⃣ 문단 분석 실행
    public ParagraphResponseDto analysisPargraphs(String paragraph) throws Exception {
        String threadId = createThread();
        try {
            addMessageToThread(threadId, paragraph);
            String runId = runThread(threadId, analysisAssistantId);
            for (int i = 0; i < 3; i++) {
                Thread.sleep(10000);
                ParagraphResponseDto analysisResult = getAnalysisResult(threadId);
                if (analysisResult != null) {
                    return analysisResult;
                }
            }
            return null;
        } finally {
            deleteThread(threadId);
        }
    }

    // ✅ 2️⃣ 특정 섹션 재생성 실행
    public String refreshAnalysis(String paragraph, String section) throws Exception{
        String threadId = createThread();
        try {
            addMessageToThread(threadId, "Rewrite the " + section + " for the following text:\n" + paragraph);
            String runId = runThread(threadId, refreshAssistantId);
            Thread.sleep(5000);
            return getRewrittenText(threadId);
        } finally {
            deleteThread(threadId);
        }
    }

    // 🔹 OpenAI API: 새로운 스레드 생성
    private String createThread() {
        GPTResponseDto response = WebClient.create(openAIConfig.getApiUrl() + "/threads")
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .bodyToMono(GPTResponseDto.class)
                .block();

        return response != null ? response.getId() : null;
    }

    // 🔹 OpenAI API: 스레드에 메시지 추가
    private void addMessageToThread(String threadId, String content) {
        log.info("메세지 추가...");
        GPTMessageDto requestDto = GPTMessageDto.builder()
                .role("user")
                .content(content)
                .build();

        WebClient.create(openAIConfig.getApiUrl() + "/threads/" + threadId + "/messages")
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                .header("OpenAI-Beta", "assistants=v2")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    // 🔹 OpenAI API: 스레드 실행
    private String runThread(String threadId, String assistantId) {
        log.info("스레드 실행...");
        GPTThreadRunRequestDto requestDto = GPTThreadRunRequestDto.builder()
                .assistantId(assistantId)
                .build();

        GPTResponseDto response = WebClient.create(openAIConfig.getApiUrl() + "/threads/" + threadId + "/runs")
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                .header("OpenAI-Beta", "assistants=v2")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(GPTResponseDto.class)
                .block();

        return response != null ? response.getId() : null;
    }

    // 🔹 OpenAI API: 메시지 리스트에서 분석 결과 가져오기 (Spring Retry 적용)
    public ParagraphResponseDto getAnalysisResult(String threadId) throws JsonProcessingException, InterruptedException {
        log.info("분석 결과 조회... {}", threadId);
            GPTThreadMessagesResponseDto response = getThreadMessages(threadId);

            if (response == null || response.getData().isEmpty()) {
                throw new RuntimeException("No messages received from GPT.");
            }

            GPTThreadMessagesResponseDto.MessageData firstMessage = response.getData().get(0);
            if (!"assistant".equals(firstMessage.getRole()) || firstMessage.getContent().isEmpty()) {
                log.info("The first message is not from the assistant.");
                return null;
        }

        return objectMapper.readValue(firstMessage.getContent().get(0).getText().getValue(), ParagraphResponseDto.class);
    }

    // 🔹 OpenAI API: 특정 섹션 재생성 결과 가져오기 (Spring Retry 적용)
    public String getRewrittenText(String threadId) {
        GPTThreadMessagesResponseDto response = getThreadMessages(threadId);

        if (response == null || response.getData().isEmpty()) {
            throw new RuntimeException("No messages received from GPT.");
        }

        GPTThreadMessagesResponseDto.MessageData firstMessage = response.getData().get(0);
        if (!"assistant".equals(firstMessage.getRole())) {
            throw new RuntimeException("The first message is not from the assistant.");
        }

        return firstMessage.getContent().get(0).getText().getValue();
    }

    // 🔹 OpenAI API: 메시지 리스트 가져오기
    private GPTThreadMessagesResponseDto getThreadMessages(String threadId) {
        return WebClient.create(openAIConfig.getApiUrl() + "/threads/" + threadId + "/messages")
                .get()
                .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .bodyToMono(GPTThreadMessagesResponseDto.class)
                .block();
    }

    // 🔹 OpenAI API: 스레드 삭제
    private void deleteThread(String threadId) {
        WebClient.create(openAIConfig.getApiUrl() + "/threads/" + threadId)
                .delete()
                .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                .header("OpenAI-Beta", "assistants=v2")
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}