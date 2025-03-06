package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class OpenAIConfig {

    @Value("${openai.apiKey}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.url}")
    private String apiUrl;
}
