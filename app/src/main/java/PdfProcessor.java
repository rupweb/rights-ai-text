import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfProcessor {
    private static final Logger log = LogManager.getLogger(PdfProcessor.class);

    public static void processPdf(File file, String outputDir) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            int totalPages = document.getNumberOfPages();
            StringBuilder fullText = new StringBuilder();
    
            if (totalPages <= 15) {
                log.info("Processing full PDF ({} pages)", totalPages);
                String text = TextExtractor.extractTextUsingDocumentAI(file);
                fullText.append(text);
            } else {
                // Split into chunks of 15 pages
                log.info("Splitting PDF into chunks ({} pages)", totalPages);
                Splitter splitter = new Splitter();
                splitter.setSplitAtPage(15);
                List<PDDocument> chunks = splitter.split(document);

                int partNumber = 1;
                for (PDDocument chunk : chunks) {
                    File tempFile = File.createTempFile("chunk_" + partNumber, ".pdf");
                    chunk.save(tempFile);
                    chunk.close();

                    String text = TextExtractor.extractTextUsingDocumentAI(tempFile);
                    fullText.append(text).append("\n\n");

                    log.info("Processed chunk {}", partNumber);
                    tempFile.delete();
                    partNumber++;
                }
            }

            // Save final aggregated output
            String outputFileName = file.getName().replaceAll("\\.pdf$", ".txt");
            String outputFilePath = outputDir + File.separator + outputFileName;
            OutputSaver.saveTextToFile(fullText.toString(), outputFilePath);
            log.info("Saved combined output to {}", outputFilePath);

        } catch (Exception e) {
            log.error("Error processing PDF file: {}", file.getAbsolutePath(), e);
            throw e;
        }
    }
}
