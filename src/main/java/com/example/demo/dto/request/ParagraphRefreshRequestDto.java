package com.example.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParagraphRefreshRequestDto {
    private Long id;
    private String section;

    private String prompt;
}