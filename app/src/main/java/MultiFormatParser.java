import java.io.*;
import java.nio.file.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import com.aspose.email.MailMessage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MultiFormatParser {
    private static final Logger log = LogManager.getLogger(MultiFormatParser.class);

    static final String defaultSource = "D:\\Documents\\Will\\Aggregated Documents\\source docs";

    public static void main(String[] args) throws IOException {
        String sourceDir = System.getProperty("sourceDir", defaultSource);
        String outputDir = sourceDir + "\\texts";

        // Create output directory if it doesn't exist
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            throw new IOException("Failed to create output directory: " + outputDir);
        }

        File folder = new File(sourceDir);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            log.warn("No files found in {}", sourceDir);
            return;
        }

        for (File file : files) {
            log.info("Processing file: {}", file.getAbsolutePath());
            processFile(file, outputDir);
        }
    }

    public static void processFile(File file, String outputDir) {
        try {
            String fileName = file.getName().toLowerCase();
            String text = "";

            if (fileName.endsWith(".pdf")) {
                text = extractTextFromPdf(file);
            } else if (fileName.endsWith(".doc")) {
                text = extractTextFromDoc(file);
            } else if (fileName.endsWith(".docx")) {
                text = extractTextFromDocx(file);
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                text = extractTextFromImage(file);
            } else if (fileName.endsWith(".msg")) {
                text = extractTextFromMsg(file);
            } else if (fileName.endsWith(".txt")) {
                text = new String(Files.readAllBytes(file.toPath()));
            } else {
                log.warn("Unsupported file type: {}", fileName);
                return;
            }

            String outputFileName = file.getName().replaceAll("\\.[^.]+$", ".txt");
            String outputFilePath = outputDir + File.separator + outputFileName;
            saveTextToFile(text, outputFilePath);
        } catch (Exception e) {
            log.error("Error processing file: {}", file.getAbsolutePath(), e);
        }
    }

    private static String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            return new org.apache.pdfbox.text.PDFTextStripper().getText(document);
        }
    }

    private static String extractTextFromDoc(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private static String extractTextFromDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private static String extractTextFromImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        // Placeholder for OCR library call
        return "[Image text extraction not implemented]";
    }

    private static String extractTextFromMsg(File file) throws IOException {
        MailMessage message = MailMessage.load(file.getAbsolutePath());
        return message.getBody() + "\n" + message.getHtmlBody();
    }

    private static void saveTextToFile(String text, String outputPath) throws IOException {
        log.info("Saving text to {}", outputPath);
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(text);
        }
    }
}
