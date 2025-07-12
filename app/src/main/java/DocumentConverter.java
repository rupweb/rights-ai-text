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

            PDFont font = new PDType1Font(FontName.HELVETICA);
            float fontSize = 12;
            float leading = 1.5f * fontSize;

            PDPage page = new PDPage(PDRectangle.A4);
            pdfDocument.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page);

            float margin = 50;
            float yStart = PDRectangle.A4.getHeight() - margin;
            float yPosition = yStart;
            float width = PDRectangle.A4.getWidth() - 2 * margin;

            contentStream.setFont(font, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);

            for (XWPFParagraph para : document.getParagraphs()) {
                String[] words = filterUnsupportedCharacters(para.getText()).split(" ");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    String testLine = line + word + " ";
                    float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
                    if (textWidth > width) {
                        contentStream.showText(line.toString());
                        contentStream.newLineAtOffset(0, -leading);
                        yPosition -= leading;
                        line = new StringBuilder(word + " ");

                        // New page if needed
                        if (yPosition <= margin) {
                            contentStream.endText();
                            contentStream.close();

                            page = new PDPage(PDRectangle.A4);
                            pdfDocument.addPage(page);
                            contentStream = new PDPageContentStream(pdfDocument, page);
                            contentStream.setFont(font, fontSize);
                            yPosition = yStart;
                            contentStream.beginText();
                            contentStream.newLineAtOffset(margin, yPosition);
                        }
                    } else {
                        line.append(word).append(" ");
                    }
                }
                if (!line.isEmpty()) {
                    contentStream.showText(line.toString());
                    contentStream.newLineAtOffset(0, -leading);
                    yPosition -= leading;
                }
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
