import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.Document.TextAnchor.TextSegment;

public class TextReconstructor {

    public static String reconstructText(Document document) {
        if (document == null || document.getText() == null || document.getText().isEmpty()) {
            return "";
        }

        List<AnnotatedSegment> segments = new ArrayList<>();

        // Collect all annotated segments across all blocks and paragraphs
        for (Document.Page page : document.getPagesList()) {
            for (Document.Page.Paragraph para : page.getParagraphsList()) {
                Document.TextAnchor anchor = para.getLayout().getTextAnchor();
                for (TextSegment segment : anchor.getTextSegmentsList()) {
                    segments.add(new AnnotatedSegment(segment.getStartIndex(), segment.getEndIndex()));
                }
            }
        }

        // Sort segments to preserve document order
        segments.sort(Comparator.comparingLong(s -> s.start));

        // Merge overlapping or adjacent segments
        List<AnnotatedSegment> merged = new ArrayList<>();
        for (AnnotatedSegment segment : segments) {
            if (merged.isEmpty()) {
                merged.add(segment);
            } else {
                AnnotatedSegment last = merged.get(merged.size() - 1);
                if (segment.start <= last.end) {
                    last.end = Math.max(last.end, segment.end);
                } else {
                    merged.add(segment);
                }
            }
        }

        // Extract and reassemble text with bounds checking and safe casting
        StringBuilder fullText = new StringBuilder();
        int textLength = document.getText().length();
        for (AnnotatedSegment segment : merged) {
            int start = (int) Math.max(0, Math.min(segment.start, textLength));
            int end = (int) Math.max(0, Math.min(segment.end, textLength));

            if (start < end) {
                fullText.append(document.getText().substring(start, end));
                fullText.append("\n\n"); // paragraph separation
            }
        }

        return fullText.toString().replaceAll("\n{3,}", "\n\n").trim();
    }

    private static class AnnotatedSegment {
        long start;
        long end;

        AnnotatedSegment(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}

