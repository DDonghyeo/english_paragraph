package com.example.demo.service;
import com.example.demo.auth.AuthUser;
import com.example.demo.dto.request.ParagraphRefreshRequestDto;
import com.example.demo.dto.response.ParagraphResponseDto;
import com.example.demo.entity.Paragraph;
import com.example.demo.entity.User;
import com.example.demo.repository.ParagraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParagraphService {

    private final GPTService gptService;
    private final ParagraphRepository paragraphRepository;

    public List<Paragraph> getParagraphs(AuthUser authUser) {
        return paragraphRepository.findAllByUserId(authUser.getId());
    }

    public Paragraph getParagraphEntity(Long id) {
        return paragraphRepository.findById(id).orElseThrow();
    }

    public Long analyzeParagraph(AuthUser authUser, String paragraph) throws Exception {

        ParagraphResponseDto paragraphResponseDto = gptService.analysisPargraphs(paragraph);
        Paragraph save = paragraphRepository.save(paragraphResponseDto.toEntity(paragraph, User.builder().id(authUser.getId()).build()));
        return save.getId();

    }

    /**
     * 블록 재생성
     */
    public String refreshBlock(AuthUser authUser, ParagraphRefreshRequestDto requestDto) throws Exception{

        Paragraph paragraph = paragraphRepository.findById(requestDto.getId()).orElseThrow();
        if (!paragraph.getUser().getId().equals(authUser.getId())) {
            throw new RuntimeException();
        }

        String content = gptService.refreshAnalysis(paragraph.getTextBySection(requestDto.getSection()), requestDto.getSection());
        paragraph.updateSection(requestDto.getSection(), content);
        paragraphRepository.save(paragraph);
        return content;
    }
}
