import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DocxToPdfConverterTest {

    private static final String TEST_SOURCE_DIR = "G:\\Petition\\rights-ai-text\\test source";

    @Test
    public void testDocxToPdfSinglePageOutput() throws IOException {
        File docxFile = new File(TEST_SOURCE_DIR, "test-input.docx");
        File pdfFile;

        // 1. Create a simple DOCX with several paragraphs
        try (XWPFDocument docx = new XWPFDocument()) {
            for (int i = 0; i < 10; i++) {
                XWPFParagraph p = docx.createParagraph();
                p.createRun().setText("Paragraph number " + (i + 1));
            }
            try (FileOutputStream out = new FileOutputStream(docxFile)) {
                docx.write(out);
            }
        }

        // 2. Convert to PDF
        pdfFile = DocumentConverter.convertDocxToPdf(docxFile);

        // 3. Load and check the number of pages
        try (PDDocument pdf = Loader.loadPDF(pdfFile)) {
            int pageCount = pdf.getNumberOfPages();
            System.out.println("Page count: " + pageCount);
            // Assert the paragraph-per-page bug is gone
            assertTrue(pageCount <= 2, "PDF should be 1 or 2 pages, not one per paragraph");
        }
    }
}
