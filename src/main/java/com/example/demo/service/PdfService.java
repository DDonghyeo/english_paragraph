package com.example.demo.service;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfService {

    @Value("${pdf_path.analysis}")
    private String templateAnalysisPath;

    @Value("${pdf_path.summary}")
    private String templateSummaryPath;

    @Value("${pdf_path.review}")
    private String templateReviewPath;

    @Value("${pdf_path.font}")
    private String fontPath;


    public String generatePdf(Long id, String passage, List<String> sentences, String title, String summary,
                              String introduction, String development, String conclusion,
                              String grammarPoint, String readingPoint, List<String> wordPoint

    ) throws IOException {
        String outputPath = "output_" + id +".pdf";

        PDDocument analysisDoc = generateAnalysisPdf(templateAnalysisPath, sentences, fontPath);
        PDDocument summaryDoc = generateSummaryPdf(templateSummaryPath, fontPath, passage, title, summary, introduction, development, conclusion);
        PDDocument reviewDoc = generateReviewPdf(templateReviewPath, fontPath, passage, grammarPoint, readingPoint, wordPoint);

        mergePdfs(outputPath, analysisDoc, summaryDoc, reviewDoc);

        analysisDoc.close();
        summaryDoc.close();
        reviewDoc.close();

        return outputPath;
    }

    private PDDocument generateAnalysisPdf(String templatePath, List<String> sentences, String fontPath) throws IOException {
        PDDocument document = PDDocument.load(new File(templatePath)); // ✅ 템플릿 PDF 불러오기
        PDFont font = PDType0Font.load(document, new File(fontPath));
        int linesPerPage = 10;  // ✅ 한 페이지당 최대 줄 수
        int lineHeight = 50;    // ✅ 줄 간격
        int startY = 710;       // ✅ 시작 Y 좌표
        int maxLineLength = 85; // ✅ 최대 글자 수

        PDPage currentPage = document.getPage(0); // ✅ 첫 번째 템플릿 페이지 가져오기
        int currentLineCount = 0; // ✅ 현재 페이지에서 작성한 줄 수

        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true);
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(60, startY);

        for (String sentence : sentences) {
            List<String> wrappedLines = wrapText(sentence, maxLineLength); // ✅ 텍스트 줄바꿈 처리

            for (String line : wrappedLines) {
                // ✅ 현재 페이지의 줄 수가 10줄을 넘으면 새로운 템플릿 페이지 추가
                if (currentLineCount >= linesPerPage) {
                    contentStream.endText();
                    contentStream.close();

                    // ✅ 새로운 템플릿 PDF를 다시 로드하여 새 페이지 가져오기
                    PDDocument tempDoc = PDDocument.load(new File(templatePath));
                    PDPage newPage = tempDoc.getPage(0); // ✅ 새 템플릿 페이지 가져오기
                    document.addPage(newPage); // ✅ 기존 문서에 추가
//                    tempDoc.close(); // ✅ 임시 문서 닫기

                    currentPage = document.getPage(document.getNumberOfPages() - 1);  // 방금 추가한 페이지 가져오기
                    currentLineCount = 0;

                    contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true);
                    contentStream.setFont(font, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(60, startY);
                }

                // ✅ 텍스트 추가
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
                                          String introduction, String development, String conclusion) throws IOException {
        PDDocument document = PDDocument.load(new File(templatePath));
        PDPage page = document.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

        PDFont font = PDType0Font.load(document, new File(fontPath));
        contentStream.setFont(font, 12);
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

    private PDDocument generateReviewPdf(String templatePath, String fontPath, String passage, String grammar, String reading, List<String> wordPoint) throws IOException {
        PDDocument document = PDDocument.load(new File(templatePath));
        PDPage page = document.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

        PDFont font = PDType0Font.load(document, new File(fontPath));
        contentStream.setFont(font, 12);
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
}
