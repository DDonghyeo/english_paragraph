package com.example.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParagraphUpdateRequestDto {
    private Long id;
    private String section;

    private String content;
}