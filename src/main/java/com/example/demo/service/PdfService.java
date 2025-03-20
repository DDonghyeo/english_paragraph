package com.example.demo.service;
import com.example.demo.entity.Fonts;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfService {

    @Value("${pdf_path.analysis}")
    private String templateAnalysisPath;

    @Value("${pdf_path.summary}")
    private String templateSummaryPath;

    @Value("${pdf_path.review}")
    private String templateReviewPath;

    @Value("${pdf_path.font.notosanskr-regular}")
    private String fontPathNotoSansKrRegular;

    @Value("${pdf_path.font.notoserifkr-medium}")
    private String fontPathNotoSerifKRMedium;

    @Value("${pdf_path.font.notoserifkr-regular}")
    private String fontPathNotoSerifKRRegular;


    public String generatePdf(Long id, String passage, List<String> sentences, String title, String summary,
                              String introduction, String development, String conclusion,
                              String grammarPoint, String readingPoint, List<String> wordPoint,
                              Fonts fonts, int size

    ) throws IOException {
        String outputPath = "output_" + id +".pdf";

        String fontPath = getFontPath(fonts);

        sentences = sentences.stream()
                .map(s -> s.replaceAll("\u200B", "")) // Zero Width Space 제거
                .collect(Collectors.toList());

        PDDocument analysisDoc = generateAnalysisPdf(templateAnalysisPath, sentences, fontPath, size);
        PDDocument summaryDoc = generateSummaryPdf(templateSummaryPath, fontPath, passage, title, summary, introduction, development, conclusion, size);
        PDDocument reviewDoc = generateReviewPdf(templateReviewPath, fontPath, passage, grammarPoint, readingPoint, wordPoint, size);

        mergePdfs(outputPath, analysisDoc, summaryDoc, reviewDoc);

        analysisDoc.close();
        summaryDoc.close();
        reviewDoc.close();

        return outputPath;
    }

    private String getFontPath(Fonts fonts) {

        switch (fonts) {
            case NOTOSANSKR_REGULAR -> { return fontPathNotoSansKrRegular;}
            case NOTOSERIFKR_MEDIUM -> { return fontPathNotoSerifKRMedium;}
            case NOTOSERIFKR_REGULAR -> { return fontPathNotoSerifKRRegular;}
            default -> {return fontPathNotoSerifKRRegular; }
        }

    }

    private PDDocument generateAnalysisPdf(String templatePath, List<String> sentences, String fontPath, int size) throws IOException {
        PDDocument document = new PDDocument();
        int linesPerPage = 10;
        int lineHeight = 50;
        int startY = 710;
        int maxLineLength = 85;

        int currentLineCount = 0;
        PDPage currentPage = importTemplatePage(templatePath);
        document.addPage(currentPage);

        PDFont font;
        try (InputStream fontStream = new FileInputStream(fontPath)) {
            font = PDType0Font.load(document, fontStream, false);
        }
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.setFont(font, size);
        contentStream.beginText();
        contentStream.newLineAtOffset(60, startY);

        for (String sentence : sentences) {
            List<String> wrappedLines = wrapText(sentence, maxLineLength);

            for (String line : wrappedLines) {
                if (currentLineCount >= linesPerPage) {
                    contentStream.endText();
                    contentStream.close();

                    // 새 페이지 복제
                    currentPage = importTemplatePage(templatePath);
                    document.addPage(currentPage);
                    currentLineCount = 0;

                    contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true);
                    contentStream.setFont(font, size);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(60, startY);
                }

                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -lineHeight);
                currentLineCount++;
            }
        }

        contentStream.endText();
        contentStream.close();
        return document;
    }

    private PDDocument generateSummaryPdf(String templatePath, String fontPath, String passage, String title, String summary,
                                          String introduction, String development, String conclusion, int size) throws IOException {
        PDDocument document = PDDocument.load(new File(templatePath));
        PDPage page = document.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

        PDFont font;
        try (InputStream fontStream = new FileInputStream(fontPath)) {
            font = PDType0Font.load(document, fontStream, false);
        }
        contentStream.setFont(font, size);
        contentStream.beginText();

        int maxLineLength = 85; // 최대 글자 수 제한
        int lineHeight = 30;    // 줄 간격 설정

        // PASSAGE
        contentStream.newLineAtOffset(60, 700);
        for (String line : wrapText(passage, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        // TITLE
        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 452));
        for (String line : wrapText(title, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        // SUMMARY
        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 372));
//        contentStream.newLineAtOffset(0, -80);
        for (String line : wrapText(summary, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        // LOGIC FLOW
        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 260));
//        contentStream.newLineAtOffset(0, -120);
        for (String line : wrapText(introduction, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }
        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 180));
        for (String line : wrapText(development, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }
        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 100));
        for (String line : wrapText(conclusion, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        contentStream.endText();
        contentStream.close();

        return document;
    }

    private PDDocument generateReviewPdf(String templatePath, String fontPath, String passage, String grammar, String reading, List<String> wordPoint, int size) throws IOException {
        PDDocument document = PDDocument.load(new File(templatePath));
        PDPage page = document.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

        PDFont font;
        try (InputStream fontStream = new FileInputStream(fontPath)) {
            font = PDType0Font.load(document, fontStream, false);
        }
        contentStream.setFont(font, size);
        contentStream.beginText();

        int passageMaxLength = 52;  // ✅ Passage 최대 글자 수
        int grammarMaxLength = 18;  // ✅ Grammar 최대 글자 수
        int lineHeight = 30;        // ✅ 기본 줄 간격 설정

        // PASSAGE (최대 52자 제한)
        contentStream.newLineAtOffset(60, 700);
        for (String line : wrapText(passage, passageMaxLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        // GRAMMAR POINT (최대 18자 제한)
        contentStream.setTextMatrix(Matrix.getTranslateInstance(370, 715));
        for (String line : wrapText(grammar, grammarMaxLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        // READING POINT (줄바꿈 필요 없음)
        contentStream.setTextMatrix(Matrix.getTranslateInstance(370, 480));
        for (String line : wrapText(reading, grammarMaxLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        // WORD POINT (기존 로직 유지)
        contentStream.setTextMatrix(Matrix.getTranslateInstance(65, 233));
        for (String word : wordPoint) {
            contentStream.showText(word);
            contentStream.newLineAtOffset(0, -30);
        }

        contentStream.endText();
        contentStream.close();

        return document;
    }

    private void mergePdfs(String outputPath, PDDocument... documents) throws IOException {
        PDDocument finalDoc = new PDDocument();
        for (PDDocument doc : documents) {
            for (PDPage page : doc.getPages()) {
                finalDoc.addPage(page);
            }
        }
        finalDoc.save(outputPath);
        finalDoc.close();
    }

    private List<String> wrapText(String text, int maxLineLength) {
        List<String> lines = new ArrayList<>();

        // ✅ 먼저 \n 기준으로 큰 줄 구분
        String[] rawLines = text.split("\\r?\\n");

        for (String rawLine : rawLines) {
            String[] words = rawLine.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > maxLineLength) {
                    lines.add(currentLine.toString().trim()); // ✅ 현재 줄 추가
                    currentLine = new StringBuilder(); // ✅ 새로운 줄 시작
                }
                currentLine.append(word).append(" ");
            }
            if (!currentLine.toString().trim().isEmpty()) {
                lines.add(currentLine.toString().trim());
            }
        }

        return lines;
    }

    private PDPage importTemplatePage(String templatePath) throws IOException {
        try (PDDocument tempDoc = PDDocument.load(new File(templatePath))) {
            // 새로운 빈 문서
            PDDocument newDoc = new PDDocument();
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.appendDocument(newDoc, tempDoc);  // 완벽히 복사
            PDPage copiedPage = newDoc.getPage(0);
            return copiedPage;
        }
    }
}
