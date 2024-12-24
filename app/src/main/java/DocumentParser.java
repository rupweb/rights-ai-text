import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DocumentParser {
    private static final Logger log = LogManager.getLogger(DocumentParser.class);

    public static void processFile(File file, String outputDir) {
        try {
            if (file.isDirectory()) {
                log.warn("Skipping directory: {}", file.getAbsolutePath());
                return;
            }

            if (FileTypeChecker.isPdf(file)) {
                PdfProcessor.processPdf(file, outputDir);
            } else if (FileTypeChecker.isDocx(file)) {
                File pdfFile = DocumentConverter.convertDocxToPdf(file);
                PdfProcessor.processPdf(pdfFile, outputDir);
            } else if (FileTypeChecker.isDoc(file)) {
                File pdfFile = DocumentConverter.convertDocToPdf(file);
                if (pdfFile != null) {
                    PdfProcessor.processPdf(pdfFile, outputDir);
                } else {
                    log.warn("Skipping unsupported .doc file: {}", file.getAbsolutePath());
                }
            } else if (FileTypeChecker.isTxt(file)) {
                TextExtractor.extractTextFromTxt(file, outputDir);
            } else if (FileTypeChecker.isImage(file)) {
                ImageExtractor.extractTextFromImage(file, outputDir);
            } else if (FileTypeChecker.isMsg(file)) {
                TextExtractor.extractTextFromMsg(file, outputDir);
            } else {
                log.warn("Unsupported file type: {}", file.getName());
            }
        } catch (Exception e) {
            log.error("Error processing file: {}", file.getAbsolutePath(), e);
        }
    }
}
