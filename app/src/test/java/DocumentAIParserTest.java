import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DocumentAIParserTest {
    private static final Logger log = LogManager.getLogger(DocumentAIParserTest.class);

    private static final String TEST_SOURCE_DIR = "D:\\Documents\\Will\\Aggregated Documents\\source docs\\test";
    private File tempPdfFile;

@BeforeEach
public void setUp() throws IOException {
    // Create a temporary PDF file for testing
    tempPdfFile = new File(TEST_SOURCE_DIR, "test-document.pdf");

    // Use Apache PDFBox to create a valid PDF with text
    try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDFont pdfFont = new PDType1Font(FontName.HELVETICA);

        try (var contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(pdfFont, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("This is a test PDF file with some sample text.");
            contentStream.endText();
            contentStream.close();
        }

        document.save(tempPdfFile);
        document.close();
    }
}

    @Test
    public void testProcessSingleFile() {
        // Ensure the test file exists
        assertTrue(tempPdfFile.exists(), "Test PDF file does not exist");

        // Set the system property for the source directory
        System.setProperty("sourceDir", TEST_SOURCE_DIR);

        // Google environment variables
        System.out.println("GOOGLE_APPLICATION_CREDENTIALS: " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

        // Call the main method to process files
        try {
            DocumentAIParser.main(new String[]{});
        } catch (Exception e) {
            fail("Exception occurred while processing: " + e.getMessage());
        }

        String TEST_OUTPUT_DIR = TEST_SOURCE_DIR + "\\texts";

        // Check that output file(s) were created
        File outputFolder = new File(TEST_OUTPUT_DIR);
        File[] outputFiles = outputFolder.listFiles((dir, name) -> name.startsWith("test-document") && name.endsWith(".txt"));
        assertNotNull(outputFiles, "No output files found");
        assertTrue(outputFiles.length > 0, "Output files were not generated");

        // Validate the content of the output file(s)
        for (File outputFile : Objects.requireNonNull(outputFiles)) {
            assertTrue(outputFile.exists(), "Output file does not exist: " + outputFile.getName());

            try {
                String content = Files.readString(Path.of(outputFile.getAbsolutePath()));
                assertFalse(content.isEmpty(), "Output file is empty: " + outputFile.getName());
            } catch (IOException e) {
                fail("Failed to read output file: " + outputFile.getName());
            }
        }
    }
}

