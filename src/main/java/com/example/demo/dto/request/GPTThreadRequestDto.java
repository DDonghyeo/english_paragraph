package com.example.demo.dto.request;

import com.example.demo.dto.GPTMessageDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // ✅ null 값은 JSON에서 제외
public class GPTThreadRequestDto {

    // 사용할 모델 (필수)
    @JsonProperty("model")
    private String model;

    // 메시지 리스트 (필수)
    @JsonProperty("messages")
    private List<GPTMessageDto> messages;

    // Assistant ID (Assistants API 사용 시 필수)
    @JsonProperty("assistant_id")
    private String assistantId;

    // Function Calling 등을 위한 tools (필수 X)
    @JsonProperty("tools")
    private List<Map<String, Object>> tools;

    // 특정 thread 실행 시 thread_id (필수 X)
    @JsonProperty("thread_id")
    private String threadId;

    // 특정 실행(run)의 결과를 가져올 때 사용되는 run_id (필수 X)
    @JsonProperty("run_id")
    private String runId;

    // 온도: 0~2 사이 값 (높을수록 무작위, 낮을수록 집중적/결정적)
    @JsonProperty("temperature")
    private Double temperature;

    // 생성할 때 사용할 토큰의 최대 길이 (필수 X)
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    // 확률 질량 기반 샘플링 설정 (필수 X, 기본값 1)
    @JsonProperty("top_p")
    private Double topP;

    // 동일한 문장을 반복할 가능성 줄이기 (필수X, 기본값 0)
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    // 새로운 주제 이야기 가능성 증가 (필수X, 기본값 0)
    @JsonProperty("presence_penalty")
    private Double presencePenalty;
}