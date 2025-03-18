package com.example.demo.controller;

import com.example.demo.annotation.CurrentUser;
import com.example.demo.auth.AuthUser;
import com.example.demo.dto.request.ParagraphRefreshRequestDto;
import com.example.demo.dto.request.ParagraphRequestDto;
import com.example.demo.entity.Paragraph;
import com.example.demo.service.ParagraphService;
import com.example.demo.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/paragraph")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ParagraphController {

    private final ParagraphService paragraphService;
    private final PdfService pdfService;

    @GetMapping("/brief")
    public ResponseEntity<?> getParagraphsBriefed(@CurrentUser AuthUser authUser) {
        return ResponseEntity.ok(paragraphService.getBriefedParagraphs(authUser));
    }

    @GetMapping("")
    public ResponseEntity<?> getParagraphs(@CurrentUser AuthUser authUser) {
        return ResponseEntity.ok(paragraphService.getParagraphs(authUser));
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeParagraph(@CurrentUser AuthUser authUser,
                                                @RequestBody ParagraphRequestDto requestDto) throws Exception {
        String paragraph = requestDto.getText();
        if (paragraph == null || paragraph.isBlank()) {
            throw new RuntimeException("영어 문단이 비어있습니다.");
        }

        // 여러 블록 생성
        Long id = paragraphService.createParagraph(authUser, paragraph);
        paragraphService.analyzeParagraph(id);
        return ResponseEntity.ok(id);
    }

    @PostMapping("/refresh")
    public String refreshBlock(@CurrentUser AuthUser authUser,
                               @RequestBody ParagraphRefreshRequestDto paragraphRefreshRequestDto) throws Exception{

        return paragraphService.refreshBlock(authUser, paragraphRefreshRequestDto);
    }

    @GetMapping("/generatePdf")
    public ResponseEntity<byte[]> generatePdf(@CurrentUser AuthUser authUser, @RequestParam("id") Long id) throws IOException {
        Paragraph paragraphEntity = paragraphService.getParagraphEntity(id);

        if (!authUser.getId().equals(paragraphEntity.getUser().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // PDF 파일 생성
        String outputPath = pdfService.generatePdf(
                paragraphEntity.getId(), paragraphEntity.getParagraph(),
                paragraphEntity.getSentenceList(), paragraphEntity.getTitle(),
                paragraphEntity.getSummary(), paragraphEntity.getIntroduction(),
                paragraphEntity.getDevelopment(), paragraphEntity.getConclusion(),
                paragraphEntity.getGrammarPoint(), paragraphEntity.getReadingPoint(),
                paragraphEntity.getWordPointList()
        );

        // 파일을 byte[]로 변환
        Path path = Paths.get(outputPath);
        byte[] pdfBytes = Files.readAllBytes(path);

        // HTTP 응답 설정
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)  // 명확한 Content-Type 지정
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdfBytes.length)) // 파일 크기 명시
                .body(pdfBytes);
    }

}
