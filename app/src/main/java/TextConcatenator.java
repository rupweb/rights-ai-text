import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

public class TextConcatenator {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java TextConcatenator <inputDir> <outputFile>");
            System.exit(1);
        }

        File inputDir = new File(args[0]);
        File outputFile = new File(args[1]);

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.err.println("Input directory does not exist or is not a directory.");
            System.exit(2);
        }

        File[] textFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (textFiles == null || textFiles.length == 0) {
            System.err.println("No .txt files found in input directory.");
            System.exit(3);
        }

        Arrays.sort(textFiles, Comparator.comparingLong(File::lastModified));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (File file : textFiles) {
                String content = Files.readString(file.toPath()).trim();
                writer.write(content);
                writer.write("\n\n-------\n\n");
            }
        }

        System.out.println("✅ Combined " + textFiles.length + " files into: " + outputFile.getAbsolutePath());
    }
}
