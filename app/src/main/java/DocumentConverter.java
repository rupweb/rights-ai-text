import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class DocumentConverter {
    private static final Logger log = LogManager.getLogger(DocumentConverter.class);

    public static File convertDocToPdf(File docFile) throws IOException {
        File pdfFile = new File(docFile.getParent(), docFile.getName().replaceAll("\\.doc$", ".pdf"));
        try (HWPFDocument doc = new HWPFDocument(new FileInputStream(docFile));
             PDDocument pdfDocument = new PDDocument()) {

            PDFont pdfFont = new PDType1Font(FontName.HELVETICA);
            // PDFont pdfFont = loadFont(pdfDocument, "NotoSans-Regular.ttf");
    
            for (String paragraph : new WordExtractor(doc).getParagraphText()) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdfDocument.addPage(page);
    
                try (PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page)) {
                    contentStream.beginText();
                    contentStream.setFont(pdfFont, 12);
                    contentStream.newLineAtOffset(100, 700);
                    contentStream.showText(filterUnsupportedCharacters(paragraph));
                    contentStream.endText();
                }
            }
            pdfDocument.save(pdfFile);
        } catch (IllegalArgumentException e) {
            log.error("Unsupported .doc file format: {}", docFile.getAbsolutePath(), e);
            return null;
        }
        return pdfFile;
    }
    
    public static File convertDocxToPdf(File docxFile) throws IOException {
        File pdfFile = new File(docxFile.getParent(), docxFile.getName().replaceAll("\\.docx$", ".pdf"));
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(docxFile));
            PDDocument pdfDocument = new PDDocument()) {

            PDFont pdfFont = new PDType1Font(FontName.HELVETICA);
            float fontSize = 12;
            float leading = 1.5f * fontSize;

            PDPage page = new PDPage(PDRectangle.A4);
            pdfDocument.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page);
            contentStream.beginText();
            contentStream.setFont(pdfFont, fontSize);
            contentStream.newLineAtOffset(50, PDRectangle.A4.getHeight() - 50);

            float yPosition = PDRectangle.A4.getHeight() - 50;

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = filterUnsupportedCharacters(paragraph.getText());
                if (text == null || text.isBlank()) continue;

                yPosition -= leading;
                if (yPosition <= 50) {
                    contentStream.endText();
                    contentStream.close();

                    page = new PDPage(PDRectangle.A4);
                    pdfDocument.addPage(page);
                    contentStream = new PDPageContentStream(pdfDocument, page);
                    contentStream.beginText();
                    contentStream.setFont(pdfFont, fontSize);
                    contentStream.newLineAtOffset(50, PDRectangle.A4.getHeight() - 50);
                    yPosition = PDRectangle.A4.getHeight() - 50 - leading;
                }

                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText(text);
            }

            contentStream.endText();
            contentStream.close();

            pdfDocument.save(pdfFile);
        }

        return pdfFile;
    }

    private static String filterUnsupportedCharacters(String text) {
        // Replace unsupported control characters with a space or remove them
        return text
        .replace("\u2610", "[ ]")
        .replace("\u2612", "[X]")
        .replaceAll("\\p{Cntrl}", " ") // Replace other control characters
        .trim();
    }
    
    private static PDFont loadFont(PDDocument pdfDocument, String fontFileName) throws IOException {
        try (InputStream fontStream = DocumentConverter.class.getClassLoader().getResourceAsStream(fontFileName)) {
            if (fontStream == null) {
                throw new IOException("Font file not found in resources: " + fontFileName);
            }
            return PDType0Font.load(pdfDocument, fontStream);
        }
    }
}
