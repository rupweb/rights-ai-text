import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
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

import com.aspose.email.MailMessage;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.cloud.documentai.v1.RawDocument;
import com.google.protobuf.ByteString;

public class MultiFormatDocumentAIParser {
    private static final Logger log = LogManager.getLogger(MultiFormatDocumentAIParser.class);

    static final String projectId = "woven-edge-445419-a8";
    static final String location = "eu";
    static final String processorId = "282be7b8a411c0ca";
    static final String credentials = "D:\\Documents\\Will\\Aggregated Documents\\woven-edge-445419-a8-ccd69cd2a32e.json";
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
            if (file.isDirectory()) {
                log.warn("Skipping directory: {}", file.getAbsolutePath());
                return;            }

            if (isPdf(file)) {
                processPdf(file, outputDir);
            } else if (isDocx(file)) {
                File pdfFile = convertDocxToPdf(file);
                processPdf(pdfFile, outputDir);
            } else if (isDoc(file)) {
                File pdfFile = convertDocToPdf(file);
                if (pdfFile != null) {
                    processPdf(pdfFile, outputDir);
                } else {
                    log.warn("Skipping unsupported .doc file: {}", file.getAbsolutePath());
                }
            }
             else if (isTxt(file)) {
                String text = extractTextFromTxt(file);
                saveTextToFile(text, new File(outputDir, file.getName().replaceAll("\\.txt$", ".txt")).getAbsolutePath());
            } else if (isImage(file)) {
                String text = extractTextFromImage(file);
                saveTextToFile(text, new File(outputDir, file.getName().replaceAll("\\.(jpg|png)$", ".txt")).getAbsolutePath());
            } else if (isMsg(file)) {
                String text = extractTextFromMsg(file);
                saveTextToFile(text, new File(outputDir, file.getName().replaceAll("\\.msg$", ".txt")).getAbsolutePath());
            } else {
                log.warn("Unsupported file type: {}", file.getName());
            }
        } catch (Exception e) {
            log.error("Error processing file: {}", file.getAbsolutePath(), e);
        }
    }

    private static boolean isDoc(File file) {
        return file.getName().toLowerCase().endsWith(".doc");
    }    

    private static boolean isPdf(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    private static boolean isDocx(File file) {
        return file.getName().toLowerCase().endsWith(".docx");
    }

    private static boolean isTxt(File file) {
        return file.getName().toLowerCase().endsWith(".txt");
    }

    private static boolean isImage(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

    private static boolean isMsg(File file) {
        return file.getName().toLowerCase().endsWith(".msg");
    }

    private static String extractTextUsingDocumentAI(File file) throws IOException {
        String endpoint = String.format("%s-documentai.googleapis.com:443", location);

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(
                DocumentProcessorServiceSettings.newBuilder()
                        .setEndpoint(endpoint)
                        .build())) {

            ByteString content = ByteString.readFrom(new FileInputStream(file));

            RawDocument rawDocument = RawDocument.newBuilder()
                    .setContent(content)
                    .setMimeType(Files.probeContentType(file.toPath()))
                    .build();

            String processorName = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName)
                    .setRawDocument(rawDocument)
                    .build();

            ProcessResponse response = client.processDocument(request);
            Document document = response.getDocument();
            return document.getText();
        } catch (Exception e) {
            log.error("Failed to extract text using Document AI for file: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }
    
    private static void processPdf(File file, String outputDir) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            int totalPages = document.getNumberOfPages();
    
            if (totalPages <= 15) {
                // Process the entire PDF if it's 15 pages or less
                String text = extractTextUsingDocumentAI(file);
                String outputFileName = file.getName().replaceAll("\\.pdf$", ".txt");
                String outputFilePath = outputDir + File.separator + outputFileName;
                saveTextToFile(text, outputFilePath);
                log.info("Processed and saved: {}", outputFilePath);
            } else {
                // Split the PDF if it has more than 15 pages
                Splitter splitter = new Splitter();
                splitter.setSplitAtPage(15);
                List<PDDocument> chunks = splitter.split(document);
    
                int partNumber = 1;
                for (PDDocument chunk : chunks) {
                    File tempFile = File.createTempFile("chunk_" + partNumber, ".pdf");
                    chunk.save(tempFile);
                    chunk.close();
    
                    String text = extractTextUsingDocumentAI(tempFile);
                    String outputFileName = file.getName().replaceAll("\\.pdf$", "_part" + partNumber + ".txt");
                    String outputFilePath = outputDir + File.separator + outputFileName;
                    saveTextToFile(text, outputFilePath);
    
                    log.info("Processed and saved part {}: {}", partNumber, outputFilePath);
                    tempFile.delete(); // Clean up temporary file
                    partNumber++;
                }
            }
        } catch (Exception e) {
            log.error("Error processing large PDF file: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }    

    private static File convertDocxToPdf(File docxFile) throws IOException {
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
                    contentStream.showText(paragraph.getText());
                    contentStream.endText();
                }
            }

            pdfDocument.save(pdfFile);
        }

        return pdfFile;
    }

    private static File convertDocToPdf(File docFile) throws IOException {
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
                    contentStream.showText(paragraph.trim());
                    contentStream.endText();
                }
            }
            pdfDocument.save(pdfFile);
        } catch (IllegalArgumentException e) {
            log.error("Unsupported .doc file format: {}", docFile.getAbsolutePath(), e);
            return null; // Return null to indicate failure
        }
        return pdfFile;
    }
    

    private static String extractTextFromTxt(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private static String extractTextFromImage(File imageFile) {
        // Placeholder for OCR implementation
        return "[Image text extraction not implemented]";
    }

    private static String extractTextFromMsg(File msgFile) throws IOException {
        MailMessage message = MailMessage.load(msgFile.getAbsolutePath());
        return message.getBody() + "\n" + message.getHtmlBody();
    }

    private static void saveTextToFile(String text, String outputPath) throws IOException {
        log.info("Saving text to {}", outputPath);
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(text);
        }
    }
}
