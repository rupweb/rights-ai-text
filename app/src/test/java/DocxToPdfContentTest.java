import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocxToPdfContentTest {

    private static final String TEST_SOURCE_DIR = "G:\\Petition\\rights-ai-text\\test source";

    @Test
    public void testDocxToPdfContainsAllWords() throws Exception {
        // 1. Construct a long paragraph
        String longParagraph = "This is a long paragraph that should wrap across lines in the PDF. "
                + "It includes multiple sentences, punctuation marks, and different word lengths to test proper rendering. "
                + "Every word should appear in the converted PDF text so that nothing is lost or truncated.";

        File docxFile = new File(TEST_SOURCE_DIR, "docx-to-pdf-word-check.docx");

        // 2. Write DOCX
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph para = doc.createParagraph();
            para.createRun().setText(longParagraph);
            try (FileOutputStream out = new FileOutputStream(docxFile)) {
                doc.write(out);
            }
        }

        assertTrue(docxFile.exists(), "DOCX file should exist");

        // 3. Convert to PDF
        File pdfFile = DocumentConverter.convertDocxToPdf(docxFile);
        assertTrue(pdfFile.exists(), "PDF file should be generated");

        // 4. Extract PDF text using PDFBox
        String pdfText;
        try (PDDocument pdfDoc = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfText = stripper.getText(pdfDoc);
        }

        // 5. Check that all words are present
        for (String word : longParagraph.split(" ")) {
            word = word.replaceAll("[^\\w]", ""); // Strip punctuation for loose matching
            if (!word.isEmpty()) {
                assertTrue(pdfText.contains(word), "PDF is missing expected word: " + word);
            }
        }

        // Optional: Write out for manual inspection
        Files.writeString(new File(TEST_SOURCE_DIR, "docx-to-pdf-word-check-output.txt").toPath(), pdfText);
    }
}
