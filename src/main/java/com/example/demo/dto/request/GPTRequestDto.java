package com.example.demo.dto.request;
import com.example.demo.dto.GPTMessageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GPTRequestDto {

    //사용할 모델 (필수)
    @JsonProperty("model")
    private String model;

    //메세지 (필수)
    @JsonProperty("messages")
    private List<GPTMessageDto> messages;

    //온도 : 0~2 사이 값 :  높을수록 출력을 더 무작위로 만들고, 낮을수록 출력을 집중적이고 결정적으로 만듦.
    //웬만하면 건들지 않는게 좋음 (필수X, 기본값 1)
    @JsonProperty("temperature")
    private Double temperature;

    //생성할 때 사용할 토큰의 상한 값. (필수 X)
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    //온도 샘플링의 대안. top_p확률 질량을 가진 토큰의 결과를 고려. 0.1 -> 10% 확률 질량을 구성하는 토큰만 고려
    //필수 X, 바꾸려면 temperature 과 같이 바꾸는걸 권장, 기본값 1
    @JsonProperty("top_p")
    private Double topP;

    //-2~2 사이의 값. 높을 수록 기존 빈도에 따라 같은 줄을 그대로 반복할 가능성이 낮아짐. (필수X, 기본값 0)
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    //-2~2 사이의 값. 높읈 수록 새 주제에 대해 이야기 할 가능성 높아짐 (필수X, 기본값 0)
    @JsonProperty("presence_penalty")
    private Double presencePenalty;



}