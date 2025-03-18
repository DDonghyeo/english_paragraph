package com.example.demo.dto.response;

import com.example.demo.entity.Paragraph;
import com.example.demo.entity.ParagraphStatus;
import com.example.demo.entity.User;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParagraphResponseDto {
    private String title;
    private String summary;
    private String introduction;
    private String development;
    private String conclusion;
    private String grammarPoint;
    private String readingPoint;
    private List<String> sentences;
    private List<String> wordPoints;

    public Paragraph toEntity(String paragraph, User user) {
        return Paragraph.builder()
                .paragraph(paragraph)
                .title(title)
                .summary(summary)
                .introduction(introduction)
                .development(development)
                .conclusion(conclusion)
                .grammarPoint(grammarPoint)
                .readingPoint(readingPoint)
                .sentenceList(sentences)
                .status(ParagraphStatus.COMPLETE)
                .wordPointList(wordPoints)
                .user(user)
                .build();
    }

    public static ParagraphResponseDto from(Paragraph paragraph) {
        return ParagraphResponseDto.builder()
                .title(paragraph.getTitle())
                .summary(paragraph.getSummary())
                .introduction(paragraph.getIntroduction())
                .development(paragraph.getDevelopment())
                .conclusion(paragraph.getConclusion())
                .grammarPoint(paragraph.getGrammarPoint())
                .readingPoint(paragraph.getReadingPoint())
                .sentences(paragraph.getSentenceList())
                .wordPoints(paragraph.getWordPointList())
                .build();
    }
}
