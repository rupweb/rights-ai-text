import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.cloud.documentai.v1.RawDocument;
import com.google.protobuf.ByteString;

public class DocumentAIParser {
    private static final Logger log = LogManager.getLogger(DocumentAIParser.class);

    static final String projectId = "woven-edge-445419-a8";
    static final String location = "eu";
    static final String processorId = "282be7b8a411c0ca";
    static final String credentials = "D:\\Documents\\Will\\Aggregated Documents\\woven-edge-445419-a8-ccd69cd2a32e.json";
    static final String defaultSource = "D:\\Documents\\Will\\Aggregated Documents\\source docs";

    public static void main(String[] args) throws IOException {
        String sourceDir = System.getProperty("sourceDir");
        if (sourceDir == null || sourceDir.isEmpty()) {
            log.warn("No source directory specified. Using default: {}", defaultSource);
            sourceDir = defaultSource;
        }

        String outputDir = sourceDir + "\\texts";

        // Create output directory if it doesn't exist
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists()) {
            if (outputFolder.mkdirs()) {
                log.info("Created output directory: {}", outputDir);
            } else {
                throw new IOException("Failed to create output directory: " + outputDir);
            }
        }

        // Get list of PDF files in source directory
        File folder = new File(sourceDir);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files == null || files.length == 0) {
            log.warn("No PDF files found in {}", sourceDir);
            return;
        }

        try {
            // Process each PDF
            for (File file : Objects.requireNonNull(files)) {
                log.info("Processing file: {}", file.getAbsolutePath());
                processPdfFile(file, outputDir);
            }
        } catch (Exception e) {
            log.error("Critical failure while processing files. Terminating application.", e);
            System.exit(1);
        }
    }

    public static void processPdfFile(File pdfFile, String outputDir) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(15);
            List<PDDocument> chunks = splitter.split(document);

            int partNumber = 1;
            for (PDDocument chunk : chunks) {
                File tempFile = File.createTempFile("chunk_" + partNumber, ".pdf");
                chunk.save(tempFile);
                chunk.close();

                String text = extractTextFromPdf(tempFile.getAbsolutePath());
                String outputFileName = pdfFile.getName().replaceAll("\\.pdf$", "_part" + partNumber + ".txt");
                String outputFilePath = outputDir + File.separator + outputFileName;

                saveTextToFile(text, outputFilePath);
                log.info("Processed and saved part {}: {}", partNumber, outputFilePath);

                tempFile.delete(); // Clean up temporary file
                partNumber++;
            }
        } catch (IOException e) {
            log.error("Error processing PDF file: {}", pdfFile.getAbsolutePath(), e);
        }
    }

    public static String extractTextFromPdf(String filePath) throws IOException {
        log.info("Extracting text from {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        String endpoint = String.format("%s-documentai.googleapis.com:443", location);

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(
                DocumentProcessorServiceSettings.newBuilder()
                        .setEndpoint(endpoint)
                        // .setCredentialsProvider(() -> GoogleCredentials.fromStream(new FileInputStream(credentials)))
                        .build())) {

            ByteString content = ByteString.readFrom(new FileInputStream(filePath));

            RawDocument rawDocument = RawDocument.newBuilder()
                    .setContent(content)
                    .setMimeType("application/pdf")
                    .build();

            String processorName = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName)
                    .setRawDocument(rawDocument)
                    .build();

            ProcessResponse response = client.processDocument(request);
            Document document = response.getDocument();
            return document.getText();
        } catch (InvalidArgumentException e) {
            log.error("Error: {}", e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                log.error("Cause: {}", cause.getMessage());
            }
            throw e;
        }
    }

    public static void saveTextToFile(String text, String outputPath) throws IOException {
        log.info("Saving text to {}", outputPath);
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(text);
        }
    }
}
