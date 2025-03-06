package com.example.demo.service;
import com.example.demo.dto.BlockDto;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public byte[] generatePdfFromBlocks(List<BlockDto> blocks) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);

        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 간단히 iText 기본 폰트 사용
        // (한글 텍스트가 있다면 별도 폰트 설정 필요)
        var font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        document.setFont(font);

        for (BlockDto block : blocks) {
            // 블록 제목
            document.add(new Paragraph("[" + block.getTitle() + "]")
                    .setBold()
                    .setFontSize(12)
            );
            // 블록 내용
            document.add(new Paragraph(block.getContent())
                    .setFontSize(11)
                    .setMarginBottom(10)
            );
        }

        document.close();
        return baos.toByteArray();
    }
}
