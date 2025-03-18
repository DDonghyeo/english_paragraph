package com.example.demo.dto.response;
import com.example.demo.dto.GPTMessageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GPTResponseDto {

    @JsonProperty("id")
    String id;

    @JsonProperty("object")
    String object;

    @JsonProperty("created")
    int created;

    @JsonProperty("model")
    String model;

    @JsonProperty("choices")
    private List<Choice> choices;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        //gpt 대화 index 번호
        @JsonProperty("index")
        private int index;

        // GPT로부터 받은 메세지
        @JsonProperty("message")
        private GPTMessageDto message;
    }

}