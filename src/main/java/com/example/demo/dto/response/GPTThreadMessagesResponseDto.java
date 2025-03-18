package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class GPTThreadMessagesResponseDto {

    @JsonProperty("data")
    private List<MessageData> data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageData {
        @JsonProperty("id")
        private String id;

        @JsonProperty("role")
        private String role;

        @JsonProperty("content")
        private List<MessageContent> content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageContent {
        @JsonProperty("type")
        private String type;

        @JsonProperty("text")
        private MessageText text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageText {
        @JsonProperty("value")
        private String value;
    }
}