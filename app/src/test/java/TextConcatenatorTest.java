import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class TextConcatenatorTest {

    private static final String TEST_SOURCE_DIR = "G:\\Petition\\rights-ai-text\\test source";

    @Test
    public void testConcatenatesMultipleTextFiles() throws Exception {
        // Create input and output paths
        Path inputDir = Paths.get(TEST_SOURCE_DIR, "concat-test-input");
        Path outputFile = Paths.get(TEST_SOURCE_DIR, "concat-test-output.txt");

        // Ensure input directory exists
        Files.createDirectories(inputDir);

        // Create test files
        Path file1 = inputDir.resolve("a.txt");
        Path file2 = inputDir.resolve("b.txt");
        Path file3 = inputDir.resolve("c.txt");

        Files.writeString(file1, "This is file one.");
        Thread.sleep(10);
        Files.writeString(file2, "This is file two.");
        Thread.sleep(10);
        Files.writeString(file3, "This is file three.");

        // Run the concatenator
        TextConcatenator.main(new String[] {
            inputDir.toAbsolutePath().toString(),
            outputFile.toAbsolutePath().toString()
        });

        // Read output
        String output = Files.readString(outputFile);

        // Verify contents
        assertTrue(output.contains("This is file one."), "Should contain file one");
        assertTrue(output.contains("This is file two."), "Should contain file two");
        assertTrue(output.contains("This is file three."), "Should contain file three");

        long delimiterCount = output.lines().filter(line -> line.equals("-------")).count();
        assertTrue(delimiterCount == 3, "Should contain three delimiter lines");
    }
}
