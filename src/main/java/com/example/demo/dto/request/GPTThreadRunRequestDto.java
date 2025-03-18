package com.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class GPTThreadRunRequestDto {

    @JsonProperty("assistant_id")
    private String assistantId;
}