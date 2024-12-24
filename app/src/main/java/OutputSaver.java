import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutputSaver {
    private static final Logger log = LogManager.getLogger(OutputSaver.class);

    public static void createOutputDirectory(String outputDir) throws IOException {
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            throw new IOException("Failed to create output directory: " + outputDir);
        }
    }

    public static void saveTextToFile(String text, String output) throws IOException {
        File outputFile = new File(output);
    
        // Ensure the output directory exists
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new IOException("Failed to create output directory: " + outputFile.getParent());
        }
    
        // Write the text to the file
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(text);
        }
    }
    
}
