import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.cloud.documentai.v1.RawDocument;
import com.google.protobuf.ByteString;

public class ImageExtractor {
    private static final Logger log = LogManager.getLogger(ImageExtractor.class);

    static final String projectId = "woven-edge-445419-a8";
    static final String location = "eu";
    static final String processorId = "282be7b8a411c0ca";

    public static String extractTextUsingDocumentAI(File file) throws IOException {
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
    
    public static void extractTextFromImage(File imageFile, String outputDir) throws IOException {
        String text = "[Image text extraction not implemented]";
        String outputFileName = imageFile.getName().replaceAll("\\.(jpg|jpeg|png)$", ".txt");
        String outputFilePath = outputDir + File.separator + outputFileName;
        OutputSaver.saveTextToFile(text, outputFilePath);
    }
}