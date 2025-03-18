package com.example.demo.dto.response;

import com.example.demo.entity.Paragraph;
import com.example.demo.entity.User;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
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
                .wordPointList(wordPoints)
                .user(user)
                .build();
    }
}
