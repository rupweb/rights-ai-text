import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
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
    
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdfDocument.addPage(page);
    
                try (PDPageContentStream contentStream = new PDPageContentStream(pdfDocument, page)) {
                    contentStream.beginText();
                    contentStream.setFont(pdfFont, 12);
                    contentStream.newLineAtOffset(100, 700);
                    contentStream.showText(filterUnsupportedCharacters(paragraph.getText()));
                    contentStream.endText();
                }
            }
    
            pdfDocument.save(pdfFile);
        }
    
        return pdfFile;
    }    

    private static String filterUnsupportedCharacters(String text) {
        // Replace unsupported control characters with a space or remove them
        return text.replaceAll("\\p{Cntrl}", " ").trim();
    }
    
}
