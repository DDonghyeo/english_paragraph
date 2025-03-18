package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GPTMessageDto {

    //역할. system(시스템), user(사용자), developer(개발자), assistant(도우미), tool(도구)
    @JsonProperty("role")
    private String role;

    //내용
    @JsonProperty("content")
    private String content;
}
