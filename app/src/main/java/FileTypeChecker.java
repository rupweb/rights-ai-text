import java.io.File;

public class FileTypeChecker {
    public static boolean isPdf(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    public static boolean isDocx(File file) {
        return file.getName().toLowerCase().endsWith(".docx");
    }

    public static boolean isDoc(File file) {
        return file.getName().toLowerCase().endsWith(".doc");
    }

    public static boolean isTxt(File file) {
        return file.getName().toLowerCase().endsWith(".txt");
    }

    public static boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    public static boolean isMsg(File file) {
        return file.getName().toLowerCase().endsWith(".msg");
    }
}
