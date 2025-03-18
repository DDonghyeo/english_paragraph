package com.example.demo.dto.response;

import com.example.demo.entity.Paragraph;
import com.example.demo.entity.ParagraphStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParagraphBriefResponseDto {

    Long id;
    String title;
    String content;
    LocalDateTime createdAt;
    ParagraphStatus status;

    public static ParagraphBriefResponseDto from(Paragraph paragraph) {
        return ParagraphBriefResponseDto.builder()
                .id(paragraph.getId())
                .title(paragraph.getTitle())
                .content(paragraph.getParagraph())
                .createdAt(paragraph.getCreatedAt())
                .status(paragraph.getStatus())
                .build();
    }
}

