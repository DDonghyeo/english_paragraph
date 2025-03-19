package com.example.demo.entity;

import com.example.demo.dto.response.ParagraphResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.io.IOException;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "paragraph")
public class Paragraph extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "paragraph", columnDefinition = "LONGTEXT")
    private String paragraph;

    @Column(columnDefinition = "JSON")
    private String sentences;

    @Transient
    private List<String> sentenceList;

    @Column
    private String title;

    @Column
    private String summary;

    @Column
    private String introduction;

    @Column
    private String development;

    @Column
    private String conclusion;

    @Column
    private String grammarPoint;

    @Column
    private String readingPoint;

    @Column(columnDefinition = "JSON")
    private String wordPoints;

    @Transient
    private List<String> wordPointList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private ParagraphStatus status;

    @PrePersist
    @PreUpdate
    public void convertListToJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.sentences = objectMapper.writeValueAsString(sentenceList); // List → JSON
        this.wordPoints = objectMapper.writeValueAsString(wordPointList); // List → JSON
    }

    @PostLoad
    public void convertJsonToList() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.sentenceList = objectMapper.readValue(sentences, new TypeReference<List<String>>() {}); // JSON → List
        this.wordPointList = objectMapper.readValue(wordPoints, new TypeReference<List<String>>() {}); // JSON → List
    }

    public void updateSection(String section, String content) {
        switch (section.toLowerCase()) {
            case "title":
                this.title = content;
                break;
            case "summary":
                this.summary = content;
                break;
            case "introduction":
                this.introduction = content;
                break;
            case "development":
                this.development = content;
                break;
            case "conclusion":
                this.conclusion = content;
                break;
            case "grammarpoint":
                this.grammarPoint = content;
                break;
            case "readingpoint":
                this.readingPoint = content;
                break;
            case "sentences":
                this.sentenceList = List.of(content.split("\n"));
                break;
            case "wordpoints":
                this.wordPointList = List.of(content.split("\n"));
                break;
            default:
                throw new IllegalArgumentException("Invalid section name: " + section);
        }
    }

    public String getTextBySection(String section) {
        switch (section.toLowerCase()) {
            case "title":
                return this.title;
            case "summary":
                return this.summary;
            case "introduction":
                return this.introduction;
            case "development":
                return this.development;
            case "conclusion":
                return this.conclusion;
            case "grammarpoint":
                return this.grammarPoint;
            case "readingpoint":
                return this.readingPoint;
            case "sentences":
                return this.sentences;
            case "wordpoints":
                return this.wordPoints;
            default:
                throw new IllegalArgumentException("Invalid section name: " + section);
        }
    }

    public void update(ParagraphResponseDto paragraphResponseDto) {
        this.title = paragraphResponseDto.getTitle();
        this.sentenceList = paragraphResponseDto.getSentences();
        this.summary = paragraphResponseDto.getSummary();
        this.introduction = paragraphResponseDto.getIntroduction();
        this.development = paragraphResponseDto.getDevelopment();
        this.conclusion = paragraphResponseDto.getConclusion();
        this.wordPointList = paragraphResponseDto.getWordPoints();
        this.readingPoint = paragraphResponseDto.getReadingPoint();
        this.grammarPoint = paragraphResponseDto.getGrammarPoint();
        this.status = ParagraphStatus.COMPLETE;
    }

}
