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
                              Fonts fonts, int size) throws IOException {

        String outputPath = "output_" + id + ".pdf";
        String fontPath = getFontPath(fonts);

        // Zero Width Space 제거
        sentences = sentences.stream()
                .map(s -> s.replaceAll("\u200B", ""))
                .collect(Collectors.toList());

        // 템플릿 문서들
        try (PDDocument analysisTemplate = PDDocument.load(new File(templateAnalysisPath));
             PDDocument summaryTemplate = PDDocument.load(new File(templateSummaryPath));
             PDDocument reviewTemplate = PDDocument.load(new File(templateReviewPath))) {

            // 각각 새 문서 생성
            PDDocument analysisDoc = generateAnalysisPdf(analysisTemplate, sentences, fontPath, size);
            PDDocument summaryDoc = generateSummaryPdf(summaryTemplate, fontPath, passage, title, summary, introduction, development, conclusion, size);
            PDDocument reviewDoc = generateReviewPdf(reviewTemplate, fontPath, passage, grammarPoint, readingPoint, wordPoint, size);

            // 병합
            mergePdfs(outputPath, analysisDoc, summaryDoc, reviewDoc);

            // 사용한 문서 닫기
            analysisDoc.close();
            summaryDoc.close();
            reviewDoc.close();
        }

        return outputPath;
    }

    private String getFontPath(Fonts fonts) {
        return switch (fonts) {
            case NOTOSANSKR_REGULAR -> fontPathNotoSansKrRegular;
            case NOTOSERIFKR_MEDIUM -> fontPathNotoSerifKRMedium;
            case NOTOSERIFKR_REGULAR -> fontPathNotoSerifKRRegular;
            default -> fontPathNotoSerifKRRegular;
        };
    }

    private PDDocument generateAnalysisPdf(PDDocument templateDoc, List<String> sentences, String fontPath, int size) throws IOException {
        PDDocument document = new PDDocument();
        int linesPerPage = 10;
        int lineHeight = 50;
        int startY = 710;
        int maxLineLength = 85;

        int currentLineCount = 0;
        PDFont font;
        try (InputStream fontStream = new FileInputStream(fontPath)) {
            font = PDType0Font.load(document, fontStream, false);
        }

        // 첫 페이지 복사
        PDPage templatePage = templateDoc.getPage(0);
        PDPage currentPage = templatePage; // 템플릿에서 직접 복사하지 않고 사용
        document.addPage(currentPage);

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

                    // 새 페이지 복사
                    PDPage newPage = templateDoc.getPage(0);
                    document.addPage(newPage);
                    currentLineCount = 0;

                    contentStream = new PDPageContentStream(document, newPage, PDPageContentStream.AppendMode.APPEND, true);
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

    private PDDocument generateSummaryPdf(PDDocument templateDoc, String fontPath, String passage, String title, String summary,
                                          String introduction, String development, String conclusion, int size) throws IOException {
        PDDocument document = new PDDocument();
        PDFont font;
        try (InputStream fontStream = new FileInputStream(fontPath)) {
            font = PDType0Font.load(document, fontStream, false);
        }

        PDPage templatePage = templateDoc.getPage(0);
        document.addPage(templatePage);

        PDPageContentStream contentStream = new PDPageContentStream(document, templatePage, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.setFont(font, size);
        contentStream.beginText();

        int maxLineLength = 85;
        int lineHeight = 30;

        contentStream.newLineAtOffset(60, 700);
        for (String line : wrapText(passage, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 452));
        for (String line : wrapText(title, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 372));
        for (String line : wrapText(summary, maxLineLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        contentStream.setTextMatrix(Matrix.getTranslateInstance(60, 260));
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

    private PDDocument generateReviewPdf(PDDocument templateDoc, String fontPath, String passage, String grammar, String reading, List<String> wordPoint, int size) throws IOException {
        PDDocument document = new PDDocument();
        PDFont font;
        try (InputStream fontStream = new FileInputStream(fontPath)) {
            font = PDType0Font.load(document, fontStream, false);
        }

        PDPage templatePage = templateDoc.getPage(0);
        document.addPage(templatePage);

        PDPageContentStream contentStream = new PDPageContentStream(document, templatePage, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.setFont(font, size);
        contentStream.beginText();

        int passageMaxLength = 52;
        int grammarMaxLength = 18;
        int lineHeight = 30;

        contentStream.newLineAtOffset(60, 700);
        for (String line : wrapText(passage, passageMaxLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        contentStream.setTextMatrix(Matrix.getTranslateInstance(370, 715));
        for (String line : wrapText(grammar, grammarMaxLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

        contentStream.setTextMatrix(Matrix.getTranslateInstance(370, 480));
        for (String line : wrapText(reading, grammarMaxLength)) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
        }

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
        String[] rawLines = text.split("\\r?\\n");
        for (String rawLine : rawLines) {
            String[] words = rawLine.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > maxLineLength) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                }
                currentLine.append(word).append(" ");
            }
            if (!currentLine.toString().trim().isEmpty()) {
                lines.add(currentLine.toString().trim());
            }
        }
        return lines;
    }
}
