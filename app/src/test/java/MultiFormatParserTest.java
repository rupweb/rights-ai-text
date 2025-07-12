import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aspose.email.MailMessage;

public class MultiFormatParserTest {
        private static final Logger log = LogManager.getLogger(MultiFormatParserTest.class);

    private static final String TEST_SOURCE_DIR = "G:\\Petition\\rights-ai-text\\test source";

    @BeforeEach
    public void setUp() throws IOException {
        // Ensure the test directory exists
        File testDir = new File(TEST_SOURCE_DIR);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }

        // Set up various test files only if they don't already exist

        // DOC file
        File tempDocFile = new File(TEST_SOURCE_DIR, "test-document1.doc");
        if (!tempDocFile.exists()) {
            try (FileWriter writer = new FileWriter(tempDocFile)) {
                writer.write("This is a test DOC file.");
            }
        }
        else log.info("File {} already exists", tempDocFile.getName());

        // DOCX file
        File tempDocxFile = new File(TEST_SOURCE_DIR, "test-document2.docx");
        if (!tempDocxFile.exists()) {
            try (XWPFDocument document = new XWPFDocument()) {
                var paragraph = document.createParagraph();
                var run = paragraph.createRun();
                run.setText("This is a test DOCX file.");
                try (FileOutputStream out = new FileOutputStream(tempDocxFile)) {
                    document.write(out);
                }
            }
        }
        else log.info("File {} already exists", tempDocxFile.getName());

        // JPG file
        File tempJpgFile = new File(TEST_SOURCE_DIR, "test-image.jpg");
        if (!tempJpgFile.exists()) {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            ImageIO.write(image, "jpg", tempJpgFile);
        }
        else log.info("File {} already exists", tempJpgFile.getName());

        // TXT file
        File tempTxtFile = new File(TEST_SOURCE_DIR, "test-document3.txt");
        if (!tempTxtFile.exists()) {
            try (FileWriter writer = new FileWriter(tempTxtFile)) {
                writer.write("This is a test TXT file.");
            }
        }
        else log.info("File {} already exists", tempTxtFile.getName());
        

        // MSG file
        File tempMsgFile = new File(TEST_SOURCE_DIR, "test-message.msg");
        if (!tempMsgFile.exists()) {
            MailMessage message = new MailMessage();
            message.setBody("This is a test email message.");
            message.save(tempMsgFile.getAbsolutePath());
        }
        else log.info("File {} already exists", tempMsgFile.getName());

        // Single-page PDF
        File singlePagePdf = new File(TEST_SOURCE_DIR, "single-page.pdf");
        if (!singlePagePdf.exists()) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                PDFont pdfFont = new PDType1Font(FontName.HELVETICA);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(pdfFont, 12);
                    contentStream.newLineAtOffset(100, 700);
                    contentStream.showText("This is a single-page PDF file.");
                    contentStream.endText();
                }
                document.save(singlePagePdf);
            }
        }
        else log.info("File {} already exists", singlePagePdf.getName());
    }

    @Test
    public void testMultiFormatProcessing() throws IOException {
        System.setProperty("sourceDir", TEST_SOURCE_DIR);
        MultiFormatParser.main(new String[]{"test source"});

        // Assert that output files were created
        assertOutputFileExists("test-document1.txt");
        assertOutputFileExists("test-document2.txt");
        assertOutputFileExists("test-document3.txt");
        assertOutputFileExists("test-image.txt");
        assertOutputFileExists("test-message.txt");
        assertOutputFileExists("single-page.txt");

        log.info("Test complete");
    }

    private void assertOutputFileExists(String expectedOutputFileName) {
        File outputFolder = new File(TEST_SOURCE_DIR + "\\texts");
        File outputFile = new File(outputFolder, expectedOutputFileName);
        assertTrue(outputFile.exists(), "Output file does not exist: " + outputFile.getName());
    }
}
