package com.example.demo.service;
import com.example.demo.auth.AuthUser;
import com.example.demo.dto.request.ParagraphRefreshRequestDto;
import com.example.demo.dto.request.ParagraphUpdateRequestDto;
import com.example.demo.dto.response.ParagraphBriefResponseDto;
import com.example.demo.dto.response.ParagraphResponseDto;
import com.example.demo.entity.Paragraph;
import com.example.demo.entity.ParagraphStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.ParagraphRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParagraphService {

    private final GPTService gptService;
    private final ParagraphRepository paragraphRepository;

    public List<ParagraphBriefResponseDto> getBriefedParagraphs(AuthUser authUser) {
        return paragraphRepository.findAllByUserId(authUser.getId()).stream().map(ParagraphBriefResponseDto::from).collect(Collectors.toList());
    }

    public ParagraphResponseDto getParagraph(AuthUser authUser, Long id) {
        Paragraph paragraph = paragraphRepository.findById(id).orElseThrow();
        if (!paragraph.getUser().getId().equals(authUser.getId())) {
            throw new RuntimeException();
        }

        return ParagraphResponseDto.from(paragraph);
    }

    @Transactional
    public void deleteParagraph(AuthUser authUser, Long id) {
        Paragraph paragraph = paragraphRepository.findById(id).orElseThrow();
        if (!paragraph.getUser().getId().equals(authUser.getId())) {
            throw new RuntimeException();
        }

        paragraphRepository.delete(paragraph);
    }

    public Paragraph getParagraphEntity(Long id) {
        return paragraphRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void updateParagraph(AuthUser authUser, ParagraphUpdateRequestDto updateRequestDto) {
        Paragraph paragraph = paragraphRepository.findById(updateRequestDto.getId()).orElseThrow();
        if (!paragraph.getUser().getId().equals(authUser.getId())) {
            throw new RuntimeException();
        }

        paragraph.updateSection(updateRequestDto.getSection(), updateRequestDto.getContent());
    }

    @Transactional
    public Long createParagraph(AuthUser authUser, String paragraph){
        log.info("지문 생성");
        Paragraph save = paragraphRepository.save(
                Paragraph.builder()
                        .status(ParagraphStatus.ANALYZING)
                        .paragraph(paragraph)
                        .user(User.builder().id(authUser.getId()).build())
                        .build());
        return save.getId();
    }

    @Async
    @Transactional
    public void analyzeParagraph(Long id) throws Exception {
        log.info("분석 시작");
        Paragraph paragraphEntity = paragraphRepository.findById(id).orElseThrow();
        ParagraphResponseDto paragraphResponseDto = gptService.analysisPargraphs(paragraphEntity.getParagraph());
        paragraphEntity.update(paragraphResponseDto);
        log.info("분석 완료");
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
