import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.google.cloud.documentai.v1.Document;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class TextReconstructorTest {

    private static final String TEST_SOURCE_DIR = "G:\\Petition\\rights-ai-text\\test source";

    @Test
    public void testWordToPdfToTextExtraction() throws Exception {
        // 1. Create Word DOCX with a long paragraph
        String longParagraph = "This is a long paragraph intended to test the Document AI OCR. "
                + "It should not break into multiple pages unless absolutely necessary. "
                + "It includes a variety of sentence structures and lengths, such as this one, "
                + "to ensure the output is coherent, complete, and properly reconstructed from the PDF.";

        File docxFile = new File(TEST_SOURCE_DIR, "long-paragraph-test.docx");
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph para = doc.createParagraph();
            para.createRun().setText(longParagraph);
            try (FileOutputStream out = new FileOutputStream(docxFile)) {
                doc.write(out);
            }
        }

        assertTrue(docxFile.exists(), "DOCX file should be created");

        // 2. Convert Word DOCX to PDF
        File pdfFile = DocumentConverter.convertDocxToPdf(docxFile);
        assertTrue(pdfFile.exists(), "PDF file should be created from DOCX");

        // 3. Extract DocumentAI-parsed Document
        Document parsedDoc = TextExtractor.getDocumentAIParsedDocument(pdfFile);

        // 4. Reconstruct text
        String reconstructed = TextReconstructor.reconstructText(parsedDoc);

        // 5. Assert expected text is found
        assertTrue(reconstructed.toLowerCase().contains("document ai ocr"),
                "Should include key phrase 'Document AI OCR'");

        assertTrue(reconstructed.toLowerCase().contains("variety of sentence structures"),
                "Should include phrase 'variety of sentence structures'");

        // Optionally write to file for manual inspection
        File outFile = new File(TEST_SOURCE_DIR, "long-paragraph-test-output.txt");
        Files.writeString(outFile.toPath(), reconstructed);
    }
}
