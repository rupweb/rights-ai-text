import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiFormatParser {
    private static final Logger log = LogManager.getLogger(MultiFormatParser.class);

    static final String credentials = "D:\\Documents\\Will\\Aggregated Documents\\woven-edge-445419-a8-ccd69cd2a32e.json";
    static final String defaultSource = "G:\\Petition\\rights-ai-text\\";

    public static void main(String[] args) throws IOException {
        // Append argument to defaultSource if provided
        String sourceDir = defaultSource + args[0];
        String outputDir = sourceDir + "\\texts";

        log.info("Using source dir: {}", sourceDir);

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
            DocumentParser.processFile(file, outputDir);
        }
    }
}
