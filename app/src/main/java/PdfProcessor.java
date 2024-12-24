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
    
            if (totalPages <= 15) {
                // Process the entire PDF if it's 15 pages or less
                String text = TextExtractor.extractTextUsingDocumentAI(file);
                String outputFileName = file.getName().replaceAll("\\.pdf$", ".txt");
                String outputFilePath = outputDir + File.separator + outputFileName;
                OutputSaver.saveTextToFile(text, outputFilePath);
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
    
                    String text = TextExtractor.extractTextUsingDocumentAI(tempFile);
                    String outputFileName = file.getName().replaceAll("\\.pdf$", "_part" + partNumber + ".txt");
                    String outputFilePath = outputDir + File.separator + outputFileName;
                    OutputSaver.saveTextToFile(text, outputFilePath);
    
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
}
