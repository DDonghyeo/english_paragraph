package com.example.demo.controller;

import com.example.demo.dto.BlockDto;
import com.example.demo.dto.ParagraphRequestDto;
import com.example.demo.service.ParagraphService;
import com.example.demo.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/paragraph")
@CrossOrigin("*")
public class ParagraphController {

    private final ParagraphService paragraphService;
    private final PdfService pdfService;

    public ParagraphController(ParagraphService paragraphService, PdfService pdfService) {
        this.paragraphService = paragraphService;
        this.pdfService = pdfService;
    }

    @PostMapping("/analyze")
    public Map<String, String> analyzeParagraph(@RequestBody ParagraphRequestDto requestDto) {
        String paragraph = requestDto.getText();
        if (paragraph == null || paragraph.isBlank()) {
            throw new RuntimeException("영어 문단이 비어있습니다.");
        }

        // 여러 블록 생성
        List<BlockDto> blocks = paragraphService.analyzeParagraph(paragraph);

        // 프론트엔드 요구사항: Map<String, String>(title -> content)
        Map<String, String> result = new LinkedHashMap<>();
        for (BlockDto block : blocks) {
            result.put(block.getTitle(), block.getContent());
        }
        return result;
    }

    @PostMapping("/refresh")
    public String refreshBlock(@RequestBody Map<String, String> blockData) {
        String blockType = blockData.get("type");
        String paragraphText = blockData.getOrDefault("paragraphText", "");
        if (blockType == null || blockType.isBlank()) {
            return "블록 타입이 누락되었습니다.";
        }
        return paragraphService.refreshBlock(blockType, paragraphText);
    }

    @PostMapping("/generatePdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody Map<String, Object> requestBody) {
        List<Map<String, String>> blocksData = (List<Map<String, String>>) requestBody.get("blocks");
        if (blocksData == null || blocksData.isEmpty()) {
            throw new RuntimeException("블록 데이터가 없습니다.");
        }

        List<BlockDto> blocks = new ArrayList<>();
        for (Map<String, String> bd : blocksData) {
            String title = bd.get("title");
            String content = bd.get("content");
            blocks.add(new BlockDto(title, content));
        }

        try {
            byte[] pdfBytes = pdfService.generatePdfFromBlocks(blocks);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"paragraph-analysis.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            throw new RuntimeException("PDF 생성 실패: " + e.getMessage(), e);
        }
    }
}
