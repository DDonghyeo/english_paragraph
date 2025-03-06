package com.example.demo.service;
import com.example.demo.dto.BlockDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParagraphService {

    private final GPTService gptService;

    public ParagraphService(GPTService gptService) {
        this.gptService = gptService;
    }

    public List<BlockDto> analyzeParagraph(String paragraph) {
        List<BlockDto> blocks = new ArrayList<>();

        // 1) 주제
        String topic = gptService.generateCompletion(
                // [시스템 프롬프트]
                "You are an assistant that provides information in plain text, without filler phrases or disclaimers. " +
                        "Do not use bold or italic formatting. Output only the requested content.",
                // [사용자 프롬프트]
                "아래 영어 문단의 '주제'를 한국어로 알려줘. " +
                        "불필요한 문구 없이, 딱 주제만 간단히 써줘.\n\n" + paragraph
        );
        blocks.add(new BlockDto("topic", topic));

        // 2) 목차 (한국어)
        String tableOfContents = gptService.generateCompletion(
                "You are an assistant that provides information in plain text, without filler phrases or disclaimers. " +
                        "No bold, italic, or markdown.",
                "아래 영어 문단의 내용을 바탕으로, 한국어로 간단한 목차를 작성해줘. " +
                        "불필요한 문구 없이 요점만. 'Certainly!' 같은 표현은 쓰지 말 것.\n\n" + paragraph
        );
        blocks.add(new BlockDto("tableOfContents", tableOfContents));

        // 3) 요약 (한글)
        String summary = gptService.generateCompletion(
                "You are a bilingual assistant. Do not use bold or italic formatting. Output must be in Korean only.",
                "아래 영어 문단을 한국어로 요약해줘. 장황한 인사말이나 'Let me summarize' 같은 표현 없이, " +
                        "결과만 깔끔하게:\n\n" + paragraph
        );
        blocks.add(new BlockDto("summary", summary));

        // 4) 구문 분석
        String syntaxAnalysis = gptService.generateCompletion(
                "You are an English grammar specialist but you explain in Korean. Do not use filler phrases, disclaimers, or markdown formatting.",
                "아래 영어 문단의 각 문장을 구문 분석해줘. " +
                        "분석과 해석은 **한국어**로, **강조** 표시(**)나 'Certainly!'와 같은 불필요한 말 없이:\n\n" + paragraph
        );
        blocks.add(new BlockDto("syntaxAnalysis", syntaxAnalysis));

        // 5) 핵심 단어
        String keywords = gptService.generateCompletion(
                "You are an English teacher focusing on vocabulary, but you explain in Korean. No markdown formatting.",
                "아래 영어 문단에서 중요하다고 생각하는 핵심 단어(5~10개)를 뽑고, " +
                        "각 단어를 한국어로 간단히 설명해줘. 'Sure!' 같은 표현은 쓰지 말기:\n\n" + paragraph
        );
        blocks.add(new BlockDto("keywords", keywords));

        // 6) 문장별 분석
        String sentenceAnalysis = gptService.generateCompletion(
                "You are a bilingual assistant. Provide a plain text analysis in Korean. No filler phrases.",
                "아래 영어 문단의 각 문장을 하나씩 분석하고, 한국어로 그 의미와 구조를 간단히 설명해줘. " +
                        "추가로 강조 표시나 'Certainly!' 같은 말은 하지 말 것:\n\n" + paragraph
        );
        blocks.add(new BlockDto("sentenceAnalysis", sentenceAnalysis));

        // 7) 문제 랜덤 제작
        String questions = gptService.generateCompletion(
                "You are a bilingual English teacher. Make the questions in English, but any explanations in Korean. Do not use bold or italic text.",
                "아래 영어 문단을 바탕으로 문제를 만들어줘. 각 문제는 영어로, 해설(설명)은 한국어로. " +
                        "불필요한 서론이나 'Sure!' 같은 말 없이, 딱 문제와 해설만:\n\n" + paragraph
        );
        blocks.add(new BlockDto("questions", questions));

        return blocks;
    }

    /**
     * 블록 재생성
     */
    public String refreshBlock(String blockType, String paragraph) {
        switch (blockType) {
            case "topic":
                return gptService.generateCompletion(
                        "You are an assistant that provides information in plain text. No filler phrases or disclaimers.",
                        "아래 문단의 주제를 한국어로 다시 간단히 표현해줘. 'Certainly!' 같은 말 없이:\n\n" + paragraph
                );

            case "tableOfContents":
                return gptService.generateCompletion(
                        "You are an assistant that provides information in plain text, no markdown. Output in Korean only.",
                        "아래 영어 문단의 목차를 한국어로 다시 정리해줘. 불필요한 표현은 빼고:\n\n" + paragraph
                );

            case "summary":
                return gptService.generateCompletion(
                        "You are a bilingual assistant. Output in Korean, no filler, no disclaimers.",
                        "아래 영어 문단을 한국어로 다시 요약해줘. 강조 표시 없이:\n\n" + paragraph
                );

            case "syntaxAnalysis":
                return gptService.generateCompletion(
                        "You are an English grammar specialist but you must explain in Korean. No bold or italic.",
                        "아래 문단을 문장별로 다시 구문 분석해줘. 한국어 설명, 불필요한 문구 없이:\n\n" + paragraph
                );

            case "keywords":
                return gptService.generateCompletion(
                        "You are an English teacher focusing on vocabulary, explaining in Korean. No markdown.",
                        "아래 영어 문단에서 핵심 단어를 다시 추려줘. 한국어로 간단히 설명:\n\n" + paragraph
                );

            case "sentenceAnalysis":
                return gptService.generateCompletion(
                        "You are a bilingual assistant analyzing text in Korean. No filler phrases or disclaimers.",
                        "아래 문단의 문장별 분석을 다시 해줘. 간결히 한국어로만:\n\n" + paragraph
                );

            case "questions":
                return gptService.generateCompletion(
                        "You are a bilingual English teacher. Make questions in English, explain in Korean. No filler or disclaimers.",
                        "아래 문단 기반으로 문제를 새로 만들어줘. 질문은 영어, 해설은 한국어. 'Sure!' 같은 말은 넣지 말 것:\n\n" + paragraph
                );

            default:
                return "재생성할 블록 유형이 올바르지 않습니다.";
        }
    }
}
